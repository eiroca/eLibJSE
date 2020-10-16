/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

public class SniSSLSocketFactory extends SSLConnectionSocketFactory {

  public static final String ENABLE_SNI = "net.eiroca.sysadm.diagnostics.utils.enable_sni";

  /*
   * Implement any constructor you need for your particular application - SSLConnectionSocketFactory
   * has many variants
   */
  public SniSSLSocketFactory(final SSLContext sslContext, final HostnameVerifier verifier) {
    super(sslContext, verifier);
  }

  @Override
  public Socket createLayeredSocket(final Socket socket, final String target, final int port, final HttpContext context) throws IOException {
    final Boolean enableSniValue = (Boolean)context.getAttribute(SniSSLSocketFactory.ENABLE_SNI);
    final boolean enableSni = (enableSniValue == null) || enableSniValue;
    return super.createLayeredSocket(socket, enableSni ? target : "", port, context);
  }
}
