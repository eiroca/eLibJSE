/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.ext.library.http.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.IContext;

public class URLFetcher implements AutoCloseable {

  private static final int READ_CHUNK_SIZE = 1024;
  private static final int BUFFER_STARTSIZE = 16 * URLFetcher.READ_CHUNK_SIZE;

  private CloseableHttpClient httpClient;
  private CredentialsProvider credsProvider;
  private URLFetcherConfig config;
  private VirtualHostInterception virtualHostInterceptor;

  public int httpStatusCode = 0;
  public long responseSize = 0;
  public long headerSize = 0;

  public boolean connectionTimedOut = false;
  public boolean socketTimedOut = false;

  public long responseStartTime = 0;
  public long firstResponseStartTime = 0;
  public long readResponseStartTime = 0;
  public long connectionCloseStartTime = 0;
  public long responseEndTime = 0;
  public long firstResponseEndTime = 0;
  public long readResponseEndTime = 0;
  public long connectionCloseEndTime = 0;

  IContext context;

  public URLFetcherConfig getConfig() {
    return config;
  }

  public URLFetcher() {
  }

  public URLFetcher(final IContext context) throws URLFetcherException {
    setup(context);
  }

  public void setup(final IContext context) throws URLFetcherException {
    this.context = context;
    final URLFetcherConfig conf = new URLFetcherConfig(context);
    setup(conf);
  }

  public void setup(final URLFetcherConfig config) throws URLFetcherException {
    this.config = config;
    credsProvider = new BasicCredentialsProvider();
    httpClient = getHttpClient();
    virtualHostInterceptor = (config.virtualHost != null) ? new VirtualHostInterception(config.virtualHost) : null;

  }

  @Override
  public void close() {
    net.eiroca.library.core.Helper.close(httpClient);
    httpClient = null;
  }

  public CloseableHttpClient getHttpClient() throws URLFetcherException {
    final Builder reqConfig = RequestConfig.custom();
    reqConfig.setMaxRedirects(config.maxRedirects);
    reqConfig.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC, AuthSchemes.NTLM, AuthSchemes.DIGEST));
    reqConfig.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC));
    // set socket timeout
    if (config.maxSocketTimeout > 0) {
      reqConfig.setSocketTimeout(config.maxSocketTimeout);
    }
    if (config.maxConnectionTimeout > 0) {
      reqConfig.setConnectTimeout(config.maxConnectionTimeout);
    }
    if (config.maxConnectionRequestTimeout > 0) {
      reqConfig.setConnectionRequestTimeout(config.maxConnectionRequestTimeout);
    }
    final HttpClientBuilder httpClientBuilder = HttpClients.custom();
    httpClientBuilder.setUserAgent(config.userAgent);
    httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
    httpClientBuilder.setDefaultRequestConfig(reqConfig.build());
    if (virtualHostInterceptor != null) {
      httpClientBuilder.addInterceptorLast(virtualHostInterceptor);
    }
    httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
    // set proxy and credentials
    if (config.useProxy) {
      final HttpHost proxy = new HttpHost(config.proxyHost, config.proxyPort);
      httpClientBuilder.setProxy(proxy);
      if (config.proxyAuth) {
        final Credentials proxyCredentials = getCredentials(config.proxyUsername, config.proxyPassword);
        credsProvider.setCredentials(new AuthScope(config.proxyHost, config.proxyPort), proxyCredentials);
      }
    }
    HostnameVerifier hostVerifier;
    TrustStrategy trustStrategy;
    if (config.allowAnyCert) {
      // a Trust Strategy that allows all certificates.
      trustStrategy = new DumbTrustStrategy();
      hostVerifier = new DumbHostVerifier();
    }
    else {
      trustStrategy = null;
      hostVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
    }
    final SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
    SSLContext sslContext = null;
    SSLConnectionSocketFactory sslSocketFactory = null;
    try {
      if (trustStrategy != null) {
        sslContextBuilder.loadTrustMaterial(trustStrategy);
      }
      sslContext = sslContextBuilder.build();
    }
    catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
      context.error("Unable to build SSL context -> " + e.getMessage());
      URLFetcherException.InvalidParameters(e);
    }
    sslSocketFactory = new SniSSLSocketFactory(sslContext, hostVerifier);
    final RegistryBuilder<ConnectionSocketFactory> factory = RegistryBuilder.<ConnectionSocketFactory> create();
    factory.register("http", PlainConnectionSocketFactory.getSocketFactory());
    factory.register("https", sslSocketFactory);
    final Registry<ConnectionSocketFactory> socketFactoryRegistry = factory.build();
    final PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(new SniHttpClientConnectionOperator(socketFactoryRegistry), null, -1, TimeUnit.MILLISECONDS);
    httpClientBuilder.setConnectionManager(connMgr);
    return httpClientBuilder.build();
  }

  public String getURL() {
    return config.getURL();
  }

  private Credentials getCredentials(String user, final String password) {
    String domain = "";
    if (user != null) {
      final int idx = user.indexOf("\\");
      if (idx > 0) {
        user = user.substring(idx + 1);
        domain = user.substring(0, idx);
      }
    }
    String localhostname;
    try {
      localhostname = java.net.InetAddress.getLocalHost().getHostName();
    }
    catch (final UnknownHostException e) {
      localhostname = "localhost";
    }
    return new NTCredentials(user, password, localhostname, domain);
  }

  public void setProtocol(final String protocol) {
    config.setProtocol(protocol);
  }

  public void setURL(final URL url) {
    config.setURL(url);
    setHost(config.host);
  }

  public void setHost(final String host) {
    config.host = host;
    if (config.serverAuth) {
      final Credentials credentials = getCredentials(config.serverUsername, config.serverPassword);
      credsProvider.setCredentials(new AuthScope(host, config.port), credentials);
    }
  }

  public void setPort(final int port) {
    config.port = port;
  }

  public void setPath(final String path) {
    config.path = path;
  }

  private HttpRequestBase createRequest() throws URLFetcherException {
    final String url = config.getURL();
    HttpRequestBase httpRequest = null;
    if (URLFetcherConfig.METHOD_GET.equals(config.method)) {
      httpRequest = new HttpGet(url);
    }
    else if (URLFetcherConfig.METHOD_HEAD.equals(config.method)) {
      httpRequest = new HttpHead(url);
    }
    else if (URLFetcherConfig.METHOD_POST.equals(config.method)) {
      httpRequest = new HttpPost(url);
      // set the POST data
      if (LibStr.isNotEmptyOrNull(config.postData)) {
        try {
          final StringEntity requestEntity = new StringEntity(config.postData, config.postType);
          ((HttpPost)httpRequest).setEntity(requestEntity);
        }
        catch (final UnsupportedCharsetException uee) {
          context.info("Encoding POST data failed: ", uee);
        }
      }
    }
    if (httpRequest == null) {
      URLFetcherException.InvalidMethod(config.method);
    }
    HttpVersion httpVersion;
    if (config.httpVersion == 0) {
      httpVersion = HttpVersion.HTTP_1_1;
    }
    else {
      httpVersion = HttpVersion.HTTP_1_0;
    }
    httpRequest.setProtocolVersion(httpVersion);
    // set dynaTrace tagging header (only timer name)
    if (config.tagging) {
      httpRequest.setHeader(config.headerName, config.headerValue);
    }
    return httpRequest;
  }

  private int calculateHeaderSize(final Header[] headers) {
    int headerLength = 0;
    for (final Header header : headers) {
      headerLength += header.getName().getBytes().length;
      headerLength += header.getValue().getBytes().length;
    }
    return headerLength;
  }

  public void setMethod(final String method, final String postData) {
    config.method = method;
    config.postData = postData;
  }

  public String execute() throws URLFetcherException {
    String result = null;
    HttpRequestBase httpRequest;
    if (config.host == null) {
      URLFetcherException.InvalidHost(config.host);
    }
    httpStatusCode = 0;
    connectionTimedOut = false;
    socketTimedOut = false;
    responseStartTime = 0;
    firstResponseStartTime = 0;
    readResponseStartTime = 0;
    connectionCloseStartTime = 0;
    responseEndTime = 0;
    firstResponseEndTime = 0;
    readResponseEndTime = 0;
    connectionCloseEndTime = 0;
    // create a HTTP client and method
    responseStartTime = System.nanoTime();
    httpRequest = createRequest();
    HttpResponse response = null;
    HttpEntity entity = null;
    try {
      // connect
      firstResponseStartTime = System.nanoTime();
      response = httpClient.execute(httpRequest);
      httpStatusCode = response.getStatusLine().getStatusCode();
      firstResponseEndTime = System.nanoTime();
      // calculate header size
      headerSize = calculateHeaderSize(response.getAllHeaders());
      // read response data
      entity = response.getEntity();
      final InputStream inputStream = entity.getContent();
      if (inputStream != null) {
        int bytesRead = 0;
        final byte[] data = new byte[URLFetcher.READ_CHUNK_SIZE];
        final String charset = response.getEntity().getContentEncoding() == null ? "utf-8" : response.getEntity().getContentEncoding().getValue();
        final StringBuilder buf = new StringBuilder(URLFetcher.BUFFER_STARTSIZE);
        readResponseStartTime = System.nanoTime();
        while ((bytesRead = inputStream.read(data)) > 0) {
          buf.append(EncodingUtils.getString(data, 0, bytesRead, charset));
          responseSize += bytesRead;
        }
        readResponseEndTime = System.nanoTime();
        result = buf.toString();
      }
    }
    catch (final Exception err) {
      context.info("Requesting URL ", httpRequest.getURI(), " caused exception: ", err);
      if (err instanceof ConnectTimeoutException) {
        connectionTimedOut = true;
      }
      else if (err instanceof SocketTimeoutException) {
        socketTimedOut = true;
      }
      URLFetcherException.URLFetchFailed(err);
    }
    finally {
      // always release the connection
      connectionCloseStartTime = System.nanoTime();
      if (entity != null) {
        try {
          EntityUtils.consume(entity);
        }
        catch (final IOException e) {
        }
      }
      httpRequest.releaseConnection();
      connectionCloseEndTime = System.nanoTime();
    }
    responseEndTime = System.nanoTime();
    return result;
  }
}
