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
package net.eiroca.ext.library.http.utils;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SniSSLSocketFactory extends SSLConnectionSocketFactory {

  private static final Logger log = LoggerFactory.getLogger(SniSSLSocketFactory.class.getName());

  public static final String ENABLE_SNI = "net.eiroca.sysadm.diagnostics.utils.enable_sni";

  private final SNIServerName sniServer;
  boolean enableSni;

  /*
   * Implement any constructor you need for your particular application - SSLConnectionSocketFactory
   * has many variants
   */
  public SniSSLSocketFactory(final SSLContext sslContext, final HostnameVerifier verifier, final SNIServerName sniServer) {
    super(sslContext, verifier);
    this.sniServer = sniServer;
  }

  @Override
  protected void prepareSocket(final SSLSocket socket) throws IOException {
    final SSLParameters p = socket.getSSLParameters();
    SniSSLSocketFactory.log.debug("Parameters=" + p);
    if (!enableSni) {
      p.setServerNames(null);
    }
    else {
      if (sniServer != null) {
        final ArrayList<SNIServerName> a = new ArrayList<>();
        a.add(sniServer);
        p.setServerNames(a);
      }
    }
    socket.setSSLParameters(p);
  }

  @Override
  public Socket createLayeredSocket(final Socket socket, final String target, final int port, final HttpContext context) throws IOException {
    final Boolean enableSniValue = (Boolean)context.getAttribute(SniSSLSocketFactory.ENABLE_SNI);
    enableSni = (enableSniValue == null) || enableSniValue;
    SniSSLSocketFactory.log.debug((socket == null ? "Creating" : "Setting") + " socket for " + target + (enableSni ? " with SNI (" + sniServer + ")" : " without SNI"));
    final SSLSocket s = (SSLSocket)super.createLayeredSocket(socket, target, port, context);
    return s;
  }

}
