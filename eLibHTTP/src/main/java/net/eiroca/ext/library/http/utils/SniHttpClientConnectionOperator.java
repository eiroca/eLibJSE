/**
 *
 * Copyright (C) 1999-2019 Enrico Croce - AGPL >= 3.0
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
import java.net.InetSocketAddress;
import javax.net.ssl.SSLProtocolException;
import org.apache.http.HttpHost;
import org.apache.http.config.Lookup;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.DefaultHttpClientConnectionOperator;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SniHttpClientConnectionOperator extends DefaultHttpClientConnectionOperator {

  private static final Logger log = LoggerFactory.getLogger(URLFetcher.class.getName());

  public SniHttpClientConnectionOperator(final Lookup<ConnectionSocketFactory> socketFactoryRegistry) {
    super(socketFactoryRegistry, null, null);
  }

  @Override
  public void connect(final ManagedHttpClientConnection conn, final HttpHost host, final InetSocketAddress localAddress, final int connectTimeout, final SocketConfig socketConfig, final HttpContext context) throws IOException {
    try {
      super.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
    }
    catch (final SSLProtocolException e) {
      final Boolean enableSniValue = (Boolean)context.getAttribute(SniSSLSocketFactory.ENABLE_SNI);
      final boolean enableSni = (enableSniValue == null) || enableSniValue;
      if (enableSni && (e.getMessage() != null) && e.getMessage().equals("handshake alert:  unrecognized_name")) {
        SniHttpClientConnectionOperator.log.debug("Server received saw wrong SNI host, retrying without SNI");
        context.setAttribute(SniSSLSocketFactory.ENABLE_SNI, false);
        super.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
      }
      else {
        throw e;
      }
    }
  }
}
