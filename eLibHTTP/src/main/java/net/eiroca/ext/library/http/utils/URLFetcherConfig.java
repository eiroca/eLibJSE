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

import java.net.URL;
import java.text.MessageFormat;
import org.apache.http.entity.ContentType;
import net.eiroca.library.system.IConfig;
import net.eiroca.library.system.IContext;

public class URLFetcherConfig {

  // configuration constants
  public static final String CONFIG_URL = "url";

  private static final String CONFIG_PROTOCOL = "protocol";
  private static final String CONFIG_PORT = "port";

  public static final String CONFIG_ALLOW_ANY_CERT = "allowAnyCert";
  public static final String CONFIG_NORMALIZEHOST = "normalizeHost";
  public static final String CONFIG_HOST_FORMAT = "hostFormat";
  public static final String CONFIG_METHOD = "method";
  public static final String CONFIG_POST_DATA = "postData";
  public static final String CONFIG_USER_AGENT = "userAgent";
  public static final String CONFIG_HTTP_VERSION = "httpVersion";
  public static final String CONFIG_MAX_REDIRECTS = "maxRedirects";

  public static final String CONFIG_TAGGING = "hasTagging";
  public static final String CONFIG_TAGNAME = "tagName";
  public static final String CONFIG_TAGVALUE = "tagValue";

  public static final String CONFIG_SERVER_AUTH = "serverAuth";
  public static final String CONFIG_SERVER_USERNAME = "serverUsername";
  public static final String CONFIG_SERVER_PASSWORD = "serverPassword";
  public static final String CONFIG_SEND_AUTHORIZATION = "sendAuthorization";

  public static final String CONFIG_USE_PROXY = "useProxy";
  public static final String CONFIG_PROXY_HOST = "proxyHost";
  public static final String CONFIG_PROXY_PORT = "proxyPort";
  public static final String CONFIG_PROXY_AUTH = "proxyAuth";
  public static final String CONFIG_PROXY_USERNAME = "proxyUsername";
  public static final String CONFIG_PROXY_PASSWORD = "proxyPassword";

  public static final String CONFIG_USE_TIMEOUT = "useTimeout";
  public static final String CONFIG_MAX_SOCKET_TIMEOUT = "maxSocketTimeout";
  public static final String CONFIG_MAX_CONNECTION_TIMEOUT = "maxConnectionTimeout";
  public static final String CONFIG_MAX_CONNECTION_REQUEST_TIMEOUT = "maxConnectionRequestTimeout";

  public static final String CONFIG_HASVIRTUALHOST = "hasVirtualHost";
  public static final String CONFIG_VIRTUALHOST = "virtualHost";

  public static final String[] PROTOCOL_DEFS = {
      "http", "https"
  };
  public static final String[] HTTPVER_DEFS = {
      "1.1", "1.0"
  };
  public static final int[] PROTOCOL_PORTS = {
      80, 443
  };

  public static final String METHOD_GET = "GET";
  public static final String METHOD_POST = "POST";
  public static final String METHOD_HEAD = "HEAD";
  public static final String DEF_METHOD = URLFetcherConfig.METHOD_GET;

  private static final String DEF_HOST = "invalid.host";
  private static final String SEP;

  static {
    // SEP = System.getProperty("line.separator");
    SEP = "";
  }

  public int protocol;
  public Boolean allowAnyCert;
  public String host;
  public int port;
  public String path;
  public String query;
  public boolean normalizeHost;
  public String hostFormat;
  public String method;
  public ContentType postType = ContentType.create("application/x-www-form-urlencoded", "UTF-8");
  public String postData;
  public int httpVersion;
  public String userAgent;

  // Request Tagging
  public boolean tagging;
  public String headerName;
  public String headerValue;

  // server authentication.
  public boolean serverAuth;
  public String serverUsername;
  public String serverPassword;
  public boolean sendAuthorization = true;

  // proxy.
  public boolean useProxy;
  public String proxyHost;
  public int proxyPort;
  public boolean proxyAuth;
  public String proxyUsername;
  public String proxyPassword;

  // timeout
  public boolean useTimeout;
  public int maxSocketTimeout;
  public int maxConnectionTimeout;
  public int maxConnectionRequestTimeout;

  // follow redirect
  public int maxRedirects;

  // Virtual Host enforcement
  String virtualHost = null;

  IContext context;

  public URLFetcherConfig(final IContext context) {
    this.context = context;
    setup(context);
  }

  private int findVal(final String val, final String[] DEFS) {
    for (int i = 0; i < DEFS.length; i++) {
      if (DEFS[i].equals(val)) { return i; }
    }
    return 0;
  }

  public void setup(final IConfig context) {
    setProtocol(context.getConfigString(URLFetcherConfig.CONFIG_PROTOCOL, null));
    httpVersion = findVal(context.getConfigString(URLFetcherConfig.CONFIG_HTTP_VERSION, null), URLFetcherConfig.HTTPVER_DEFS);
    setURL(context.getConfigUrl(URLFetcherConfig.CONFIG_URL));
    port = context.getConfigInt(URLFetcherConfig.CONFIG_PORT, 0);
    allowAnyCert = context.getConfigBoolean(URLFetcherConfig.CONFIG_ALLOW_ANY_CERT, false);
    normalizeHost = context.getConfigBoolean(URLFetcherConfig.CONFIG_NORMALIZEHOST, false);
    hostFormat = context.getConfigString(URLFetcherConfig.CONFIG_HOST_FORMAT, "{0}");
    method = context.getConfigString(URLFetcherConfig.CONFIG_METHOD, URLFetcherConfig.DEF_METHOD).toUpperCase();
    postData = context.getConfigString(URLFetcherConfig.CONFIG_POST_DATA, null);
    userAgent = context.getConfigString(URLFetcherConfig.CONFIG_USER_AGENT, null);
    serverAuth = context.getConfigBoolean(URLFetcherConfig.CONFIG_SERVER_AUTH, false);
    if (serverAuth) {
      serverUsername = context.getConfigString(URLFetcherConfig.CONFIG_SERVER_USERNAME, null);
      serverPassword = context.getConfigPassword(URLFetcherConfig.CONFIG_SERVER_PASSWORD);
      sendAuthorization = context.getConfigBoolean(URLFetcherConfig.CONFIG_SEND_AUTHORIZATION, true);
    }
    useProxy = context.getConfigBoolean(URLFetcherConfig.CONFIG_USE_PROXY, false);
    if (useProxy) {
      proxyHost = context.getConfigString(URLFetcherConfig.CONFIG_PROXY_HOST, null);
      proxyPort = context.getConfigInt(URLFetcherConfig.CONFIG_PROXY_PORT, 0);
    }
    proxyAuth = context.getConfigBoolean(URLFetcherConfig.CONFIG_PROXY_AUTH, false);
    if (proxyAuth) {
      proxyUsername = context.getConfigString(URLFetcherConfig.CONFIG_PROXY_USERNAME, null);
      proxyPassword = context.getConfigPassword(URLFetcherConfig.CONFIG_PROXY_PASSWORD);
    }
    tagging = context.getConfigBoolean(URLFetcherConfig.CONFIG_TAGGING, false);
    headerName = context.getConfigString(URLFetcherConfig.CONFIG_TAGNAME, null);
    headerValue = context.getConfigString(URLFetcherConfig.CONFIG_TAGVALUE, null);
    if ((headerName == null) || (headerValue == null)) {
      tagging = false;
    }
    maxRedirects = context.getConfigInt(URLFetcherConfig.CONFIG_MAX_REDIRECTS, 0);
    if (context.getConfigBoolean(URLFetcherConfig.CONFIG_USE_TIMEOUT, false)) {
      useTimeout = true;
      maxSocketTimeout = context.getConfigInt(URLFetcherConfig.CONFIG_MAX_SOCKET_TIMEOUT, 0);
      maxConnectionTimeout = context.getConfigInt(URLFetcherConfig.CONFIG_MAX_CONNECTION_TIMEOUT, 0);
      maxConnectionRequestTimeout = context.getConfigInt(URLFetcherConfig.CONFIG_MAX_CONNECTION_REQUEST_TIMEOUT, 0);
    }
    else {
      useTimeout = false;
      maxSocketTimeout = 0;
      maxConnectionTimeout = 0;
      maxConnectionRequestTimeout = 0;
    }
    if (context.getConfigBoolean(URLFetcherConfig.CONFIG_HASVIRTUALHOST, false)) {
      virtualHost = context.getConfigString(URLFetcherConfig.CONFIG_VIRTUALHOST, null);
    }
    else {
      virtualHost = null;
    }
  }

  public void setProtocol(final String protocol) {
    this.protocol = findVal(protocol, URLFetcherConfig.PROTOCOL_DEFS);
  }

  public void setURL(final URL url) {
    if (url != null) {
      setProtocol(url.getProtocol());
      host = url.getHost();
      port = url.getPort();
      path = url.getPath();
      query = url.getQuery();
    }
  }

  public String getURL() {
    final StringBuilder url = new StringBuilder(256);
    url.append(getProcol());
    url.append("://");
    final String finalHost = host == null ? URLFetcherConfig.DEF_HOST : host;
    url.append(normalizeHost ? MessageFormat.format(hostFormat, finalHost) : finalHost);
    if (port > 0) {
      url.append(":").append(port);
    }
    if (path != null) {
      if (!path.startsWith("/")) {
        url.append("/");
      }
      url.append(path);
    }
    if (query != null) {
      if (path == null) {
        url.append('/');
      }
      url.append('?').append(query);
    }
    return url.toString();
  }

  public String getProcol() {
    return URLFetcherConfig.PROTOCOL_DEFS[protocol];
  }

  private void addSetting(final StringBuffer out, final String name, final Object val) {
    addSetting(out, name, val, false);
  }

  private void addSetting(final StringBuffer out, final String name, final Object val, final boolean last) {
    if (val != null) {
      out.append('"').append(name).append("\":");
      if (val instanceof String) {
        out.append('"').append(val.toString()).append('"');
      }
      else {
        out.append(val.toString());
      }
      if ((!last)) {
        out.append(',');
      }
      out.append(URLFetcherConfig.SEP);
    }
  }

  @Override
  public String toString() {
    final StringBuffer out = new StringBuffer(200);
    out.append("settings:{").append(URLFetcherConfig.SEP);
    if (httpVersion > 0) {
      addSetting(out, "httpVersion", httpVersion);
    }
    addSetting(out, URLFetcherConfig.CONFIG_URL, getURL());
    if (protocol == 1) {
      addSetting(out, URLFetcherConfig.CONFIG_ALLOW_ANY_CERT, allowAnyCert);
    }
    if (!URLFetcherConfig.DEF_METHOD.equals(method)) {
      addSetting(out, "method", method);
    }
    if (URLFetcherConfig.METHOD_POST.equals(method)) {
      if ((postData != null) && (postData.length() > 0)) {
        addSetting(out, URLFetcherConfig.CONFIG_POST_DATA, postData);
      }
    }
    if (normalizeHost) {
      addSetting(out, URLFetcherConfig.CONFIG_NORMALIZEHOST, normalizeHost);
      addSetting(out, URLFetcherConfig.CONFIG_HOST_FORMAT, hostFormat);

    }
    addSetting(out, URLFetcherConfig.CONFIG_USER_AGENT, userAgent);
    if (serverAuth) {
      addSetting(out, URLFetcherConfig.CONFIG_SERVER_AUTH, serverAuth);
      addSetting(out, URLFetcherConfig.CONFIG_SERVER_USERNAME, serverUsername);
      addSetting(out, URLFetcherConfig.CONFIG_SERVER_PASSWORD, serverPassword);
    }
    if (useProxy) {
      addSetting(out, URLFetcherConfig.CONFIG_USE_PROXY, useProxy);
      addSetting(out, URLFetcherConfig.CONFIG_PROXY_HOST, proxyHost);
      addSetting(out, URLFetcherConfig.CONFIG_PROXY_PORT, proxyPort);
      if (proxyAuth) {
        addSetting(out, URLFetcherConfig.CONFIG_PROXY_AUTH, proxyAuth);
        addSetting(out, URLFetcherConfig.CONFIG_PROXY_USERNAME, proxyUsername);
        addSetting(out, URLFetcherConfig.CONFIG_PROXY_PASSWORD, proxyPassword);
      }
    }
    if (useTimeout) {
      if (maxSocketTimeout > 0) {
        addSetting(out, URLFetcherConfig.CONFIG_MAX_SOCKET_TIMEOUT, maxSocketTimeout);
      }
      if (maxConnectionTimeout > 0) {
        addSetting(out, URLFetcherConfig.CONFIG_MAX_CONNECTION_TIMEOUT, maxConnectionTimeout);
      }
      if (maxConnectionRequestTimeout > 0) {
        addSetting(out, URLFetcherConfig.CONFIG_MAX_CONNECTION_REQUEST_TIMEOUT, maxConnectionRequestTimeout);
      }
    }
    if (maxRedirects > 0) {
      addSetting(out, URLFetcherConfig.CONFIG_MAX_REDIRECTS, maxRedirects);
    }
    if (tagging) {
      addSetting(out, URLFetcherConfig.CONFIG_TAGGING, tagging);
      addSetting(out, URLFetcherConfig.CONFIG_TAGNAME, headerName);
      addSetting(out, URLFetcherConfig.CONFIG_TAGVALUE, headerValue);
    }
    addSetting(out, "vers", 1, true);
    out.append("}");
    return out.toString();
  }
}
