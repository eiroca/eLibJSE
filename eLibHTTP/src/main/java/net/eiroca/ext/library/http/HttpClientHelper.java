/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.ext.library.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import net.eiroca.library.core.Helper;
import net.eiroca.library.system.Logs;

public class HttpClientHelper {

  public static Logger logger = Logs.getLogger();

  public static final String APPLICATION_JSON = "application/json;charset=%s";
  public static final String APPLICATION_XML = "application/xml;charset=%s";

  public static final CredentialsProvider credsProvider = new BasicCredentialsProvider();

  static int requestTimeout = 90000;
  static int connectTimeout = 30000;
  static int socketTimeout = 30000;

  public static CloseableHttpClient createAcceptAllClient(final HttpHost proxy) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    final SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustAllStrategy()).build();
    final HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
    final SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);
    final RequestConfig.Builder requestConfigBuilder = HttpClientHelper.getRequestBuilder(proxy);
    final HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setDefaultRequestConfig(requestConfigBuilder.build());
    builder.setDefaultCredentialsProvider(HttpClientHelper.credsProvider);
    builder.setSSLSocketFactory(connectionFactory);
    return builder.build();
  }

  public static CloseableHttpClient getHttpClient(final HttpHost proxy) {
    return HttpClientHelper.getHttpClient(proxy, null);
  }

  public static CloseableHttpClient getHttpClient(final HttpHost proxy, final Collection<Header> defHeader) {
    final RequestConfig.Builder requestConfigBuilder = HttpClientHelper.getRequestBuilder(proxy);
    final HttpClientBuilder builder = HttpClientBuilder.create();
    builder.setDefaultRequestConfig(requestConfigBuilder.build());
    builder.setDefaultCredentialsProvider(HttpClientHelper.credsProvider);
    builder.setDefaultHeaders(defHeader);
    return builder.build();
  }

  private static Builder getRequestBuilder(final HttpHost proxy) {
    final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    requestConfigBuilder.setConnectionRequestTimeout(HttpClientHelper.requestTimeout);
    requestConfigBuilder.setConnectTimeout(HttpClientHelper.connectTimeout);
    requestConfigBuilder.setSocketTimeout(HttpClientHelper.socketTimeout);
    if (proxy != null) {
      requestConfigBuilder.setProxy(proxy);
    }
    return requestConfigBuilder;
  }

  public static String GET(final CloseableHttpClient httpClient, final String url) {
    String result = null;
    final HttpGet httpMethod = new HttpGet(url);
    try (CloseableHttpResponse response = httpClient.execute(httpMethod)) {
      final int httpStatusCode = response.getStatusLine().getStatusCode();
      final HttpEntity entity = response.getEntity();
      if (httpStatusCode < 300) {
        result = EntityUtils.toString(entity, "UTF-8");
      }
      else {
        EntityUtils.consume(entity);
      }
    }
    catch (final IOException e) {
      Logs.ignore(e);
    }
    return result;
  }

  public static String POST(final CloseableHttpClient httpClient, final String url, final String doc, final ContentType type) {
    String result = null;
    final HttpPost httpMethod = new HttpPost(url);
    final HttpEntity body = new StringEntity(doc, type);
    httpMethod.setEntity(body);
    try (CloseableHttpResponse response = httpClient.execute(httpMethod)) {
      final int httpStatusCode = response.getStatusLine().getStatusCode();
      final HttpEntity entity = response.getEntity();
      if (httpStatusCode < 300) {
        result = EntityUtils.toString(entity, "UTF-8");
      }
      else {
        EntityUtils.consume(entity);
      }
    }
    catch (final IOException e) {
      Logs.ignore(e);
    }
    return result;
  }

  public static String getAuthHeader(final String user, final String password, final String secret) {
    final String code;
    if (secret != null) {
      final GoogleAuthenticator gAuth = new GoogleAuthenticator();
      final int codeNum = gAuth.getTotpPassword(secret);
      code = String.format("%06d", codeNum);
    }
    else {
      code = "";
    }
    final String auth = user + ":" + password + code;
    final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
    final String authHeader = "Basic " + new String(encodedAuth);
    return authHeader;
  }

  public static boolean appendParam(final StringBuilder sb, final String param, final String val, boolean first) {
    if (val != null) {
      if (first) {
        sb.append('?');
      }
      else {
        sb.append('&');
      }
      sb.append(param);
      sb.append('=');
      sb.append(val);
      first = false;
    }
    return first;
  }

  public static String getData(final CloseableHttpClient httpClient, final String url, final String acceptHeader, final String authHeader, final String charset) {
    String data = null;
    final HttpGet httpMethod = new HttpGet(url);
    if (acceptHeader != null) {
      httpMethod.setHeader(HttpHeaders.ACCEPT, acceptHeader);
    }
    if (charset != null) {
      httpMethod.setHeader(HttpHeaders.ACCEPT_CHARSET, charset);
    }
    if (authHeader != null) {
      httpMethod.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
    }
    CloseableHttpResponse response = null;
    try {
      response = httpClient.execute(httpMethod);
      final int httpStatusCode = response.getStatusLine().getStatusCode();
      final HttpEntity entity = response.getEntity();
      if (httpStatusCode < 400) {
        data = EntityUtils.toString(entity, (charset != null) ? charset : Helper.DEFAULT_ENCODING);
      }
      else {
        HttpClientHelper.logger.warn("Error " + httpStatusCode + " invoking " + url);
        EntityUtils.consume(entity);
      }
    }
    catch (final IOException err) {
      HttpClientHelper.logger.warn("Error invoking " + url, err);
    }
    finally {
      Helper.close(response);
    }
    return data;
  }

  public static String getJson(final CloseableHttpClient httpClient, final String url, final String authHeader, final String charset) {
    return HttpClientHelper.getData(httpClient, url, String.format(HttpClientHelper.APPLICATION_JSON, charset), authHeader, charset);
  }

  public static String getXML(final CloseableHttpClient httpClient, final String url, final String authHeader, final String charset) {
    return HttpClientHelper.getData(httpClient, url, String.format(HttpClientHelper.APPLICATION_XML, charset), authHeader, charset);
  }

  public static String consume(final HttpEntity entity) {
    String resultStr = null;
    try {
      resultStr = EntityUtils.toString(entity);
    }
    catch (ParseException | IOException e) {
      resultStr = null;
    }
    return resultStr;
  }

}
