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
package net.eiroca.library.diagnostics.monitors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import net.eiroca.library.diagnostics.CommandError;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.IServerMonitor;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.diagnostics.validators.GenericValidator;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.ContextParameters;
import net.eiroca.library.system.IContext;

public abstract class ServerMonitor implements IServerMonitor {

  // parameters
  protected static ContextParameters params = new ContextParameters();

  // Mesures
  protected MetricGroup mgMonitor = new MetricGroup(null, "Monitor");

  // measure constants
  protected MetricGroup mgServerInfo = new MetricGroup(mgMonitor, "Server Statistics");
  protected Measure mServerConnectionTimeout = mgServerInfo.createMeasure("ConnectionTimedOut", MetricAggregation.zero, "1 if connection timed out", "boolean");
  protected Measure mServerLatency = mgServerInfo.createMeasure("ServerLatency", MetricAggregation.zero, "Server latency", "ms");
  protected Measure mServerReachable = mgServerInfo.createMeasure("HostReachable", MetricAggregation.zero, "1 if host is reachable", "boolean");
  protected Measure mServerResponseTime = mgServerInfo.createMeasure("Response Time", MetricAggregation.zero, "Complete response time (e.g. connection + query + read)", "ms");
  protected Measure mServerResult = mgServerInfo.createMeasure("Result", MetricAggregation.zero, "Server Response (e.g. query data)", "number");
  protected Measure mServerSocketTimeout = mgServerInfo.createMeasure("SocketTimedOut", MetricAggregation.zero, "1 if socket had timed out", "boolean");
  protected Measure mServerStatus = mgServerInfo.createMeasure("Status", MetricAggregation.zero, "Response status (0 OK)", "boolean");
  protected Measure mServerVerified = mgServerInfo.createMeasure("ContentVerified", MetricAggregation.zero, "1 if result is validated and valid", "boolean");

  protected IContext context;

  public ServerMonitor() {
  }

  public void readConf() throws CommandException {
  }

  private InetAddress fromHostName(final String hostAddress) throws UnknownHostException {
    return InetAddress.getByName(hostAddress);
  }

  @Override
  final public boolean check(final String hostAddress) throws CommandException {
    InetAddress host;
    try {
      host = fromHostName(hostAddress);
    }
    catch (final UnknownHostException e) {
      throw new CommandException(CommandError.Configuration, e.getMessage());
    }
    return check(host);
  }

  final public boolean check(final InetAddress host) throws CommandException {
    final boolean result = preCheck(host) && runCheck() && postCheck();
    return result;
  }

  public boolean preCheck(final InetAddress host) throws CommandException {
    context.debug(getClass().getSimpleName(), " prechecks ", host.getHostAddress());
    return true;
  }

  public boolean runCheck() throws CommandException {
    ReturnObject response;
    response = fetchResponse();
    if (response != null) {
      processResponse(response);
    }
    return true;
  }

  public boolean postCheck() throws CommandException {
    context.debug(getClass().getSimpleName(), " postchecks ");
    return true;
  }

  public ReturnObject fetchResponse() throws CommandException {
    return new ReturnObject(200, null);
  }

  public void processResponse(final ReturnObject response) throws CommandException {
    context.info("Checking response");
    if (validateResponse(response, null)) {
      parseResponse(response);
    }
  }

  public boolean validateResponse(final ReturnObject response, final GenericValidator validator) throws CommandException {
    context.info("Validating response");
    final int status = response.getRetCode();
    final boolean ok = (status > 0) && (status < 400);
    final boolean isValid = (validator != null) ? validator.isValid(response.getOutput()) : true;
    final boolean hasError = !ok || !isValid;
    mServerStatus.setValue(hasError);
    mServerVerified.setValue(isValid);
    return true;
  }

  public void parseResponse(final ReturnObject response) throws CommandException {
    mServerResult.setValue(response.getRetCode());
  }

  @Override
  public void setup(final IContext context) throws CommandException {
    this.context = context;
    readConf();
    resetMetrics();
  }

  @Override
  public void resetMetrics() {
    final List<MetricGroup> groups = new ArrayList<>();
    loadMetricGroup(groups);
    for (final MetricGroup mg : groups) {
      mg.reset();
    }
  }

  @Override
  final public void loadMetricGroup(final List<MetricGroup> groups) {
    mgMonitor.loadGroups(groups, true);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(256);
    sb.append("{\"monitor\": \"").append(getClass().getSimpleName()).append("\",\"measures\":{");
    final List<MetricGroup> groups = new ArrayList<>();
    loadMetricGroup(groups);
    boolean first = true;
    for (final MetricGroup g : groups) {
      if (!first) {
        sb.append(',');
      }
      else {
        first = false;
      }
      sb.append(g);
    }
    sb.append("}}");
    return sb.toString();
  }

  @Override
  public void close() throws Exception {
  }

  @Override
  public MetricGroup getMetricGroup() {
    return mgMonitor;
  }

}
