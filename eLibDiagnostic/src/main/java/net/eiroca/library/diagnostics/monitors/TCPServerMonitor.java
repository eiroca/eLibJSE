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
package net.eiroca.library.diagnostics.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import net.eiroca.library.core.Helper;
import net.eiroca.library.diagnostics.CommandException;

public class TCPServerMonitor extends ServerMonitor {

  private static final String CONFIG_PORT = "port";
  private static final String CONFIG_USE_TIMEOUT = "useTimeout";
  private static final String CONFIG_MAX_CONNECTION_TIMEOUT = "maxConnectionTimeout";

  protected int port;
  protected int timeout;
  protected InetSocketAddress targetHost;

  @Override
  public void readConf() throws CommandException {
    port = context.getConfigInt(TCPServerMonitor.CONFIG_PORT, -1);
    if ((port < 0) || (port > 65535)) {
      CommandException.ConfigurationError("Invalid port number");
    }
    if (context.getConfigBoolean(TCPServerMonitor.CONFIG_USE_TIMEOUT, false)) {
      timeout = context.getConfigInt(TCPServerMonitor.CONFIG_MAX_CONNECTION_TIMEOUT, 0);
    }
    else {
      timeout = 0;
    }
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    final boolean ok = super.preCheck(host);
    if (ok) {
      targetHost = new InetSocketAddress(host, port);
    }
    return ok;
  }

  @Override
  public boolean runCheck() throws CommandException {
    boolean succed = false;
    final long startTime = System.nanoTime();
    long connectStartTime = startTime;
    long endTime = startTime;
    boolean socketTO = false;
    final boolean connectionTO = false;
    final boolean verified = true;
    final double result = 0;
    boolean error = true;
    String lastErr = null;
    try {
      final Socket socket = new Socket();
      if (timeout > 0) {
        socket.connect(targetHost, timeout);
      }
      else {
        socket.connect(targetHost);
      }
      connectStartTime = System.nanoTime();
      socket.close();
      succed = true;
      error = false;
    }
    catch (final UnknownHostException e) {
      CommandException.ConfigurationError("Invalid host address " + targetHost.getHostString());
    }
    catch (final SocketTimeoutException e) {
      lastErr = Helper.getExceptionAsString(e, false);
      socketTO = true;
    }
    catch (final IOException e) {
      lastErr = Helper.getExceptionAsString(e, false);
    }
    if (lastErr != null) {
      context.info("Connection to ", targetHost.getHostString(), " failed ", lastErr);
    }
    endTime = System.nanoTime();
    mServerReachable.setValue(succed);
    mServerConnectionTimeout.setValue(connectionTO);
    mServerSocketTimeout.setValue(socketTO);
    mServerLatency.setValue(Helper.elapsed(startTime, connectStartTime));
    mServerVerified.setValue(verified);
    mServerResponseTime.setValue(Helper.elapsed(startTime, endTime));
    mServerResult.setValue(result);
    mServerStatus.setValue(error);
    return true;
  }

}
