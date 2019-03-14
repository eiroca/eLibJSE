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
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.ext.library.http.utils.URLFetcherException;
import net.eiroca.library.diagnostics.CommandError;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;

public class ApacheServerMonitor extends WebServerMonitor {

  private static final String CONFIG_MODSTATUSURL = "modStatusURL";

  // measurement variables
  public MetricGroup mgApachePerformance = new MetricGroup("Apache Performance", "Apache - {0}");
  public Measure totalAccesses = mgApachePerformance.createMeasure("TotalAccesses");
  public Measure totalkBytes = mgApachePerformance.createMeasure("TotalBytes");
  public Measure cpuLoad = mgApachePerformance.createMeasure("CPULoad");
  public Measure uptime = mgApachePerformance.createMeasure("Uptime");
  public Measure reqPerSec = mgApachePerformance.createMeasure("ReqPerSec");
  public Measure bytesPerSec = mgApachePerformance.createMeasure("BytesPerSec");
  public Measure bytesPerReq = mgApachePerformance.createMeasure("BytesPerReq");
  public Measure busyWorkers = mgApachePerformance.createMeasure("BusyWorkers");
  public Measure idleWorkers = mgApachePerformance.createMeasure("IdleWorkers");
  public Measure workersUtilization = mgApachePerformance.createMeasure("WorkersUtilization");

  public MetricGroup mgApacheScoreboard = new MetricGroup("Apache Scoreboard", "Apache - Scoreboard - {0}");
  public Measure sbWaitingForConnection = mgApacheScoreboard.createMeasure("Waiting");
  public Measure sbStartingUp = mgApacheScoreboard.createMeasure("Starting up");
  public Measure sbReadingRequest = mgApacheScoreboard.createMeasure("Reading Request");
  public Measure sbSendingReply = mgApacheScoreboard.createMeasure("Sending Reply");
  public Measure sbKeepAlive = mgApacheScoreboard.createMeasure("Keepalive (read)");
  public Measure sbDnsLookup = mgApacheScoreboard.createMeasure("DNS Lookup");
  public Measure sbClosingConnection = mgApacheScoreboard.createMeasure("Closing connection");
  public Measure sbLogging = mgApacheScoreboard.createMeasure("Logging");
  public Measure sbGracefullyFinishing = mgApacheScoreboard.createMeasure("Gracefully finishing");
  public Measure sbIdleCleanupOfWorker = mgApacheScoreboard.createMeasure("Idle cleanup of worker");
  public Measure sbOpenSlot = mgApacheScoreboard.createMeasure("Open Slot");

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    final boolean ok = super.preCheck(host);
    if (ok) {
      final URL baseURL = getURL(ApacheServerMonitor.CONFIG_MODSTATUSURL, host.getHostName());
      context.info("ElasticSearch URL: " + baseURL);
      fetcher.setURL(baseURL);
      fetcher.setMethod(URLFetcherConfig.METHOD_GET, null);
    }
    return ok;
  }

  @Override
  public boolean runCheck() throws CommandException {
    if (urlCheck) {
      super.runCheck();
    }
    context.info("Loading mod_status: ", fetcher.getURL());
    String result;
    try {
      result = fetcher.execute();
    }
    catch (final URLFetcherException err) {
      final int errorCode = err.getErrorCode();
      CommandError resultError;
      final String message = String.format("Fertching error %d: %s", err.getErrorCode(), err.getMessage());
      if (errorCode < 100) {
        resultError = CommandError.Internal;
      }
      else if (errorCode < 200) {
        resultError = CommandError.Infrastructure;
      }
      else {
        resultError = CommandError.Generic;
      }
      throw new CommandException(resultError, message);
    }
    if (!mServerReachable.hasValue()) {
      final int httpStatus = fetcher.httpStatusCode;
      final boolean ok = (httpStatus > 0) && (httpStatus < 400);
      mServerReachable.setValue(httpStatus > 0);
      mServerConnectionTimeout.setValue(fetcher.connectionTimedOut);
      mServerSocketTimeout.setValue(fetcher.socketTimedOut);
      mServerLatency.setValue(net.eiroca.library.core.Helper.elapsed(fetcher.firstResponseStartTime, fetcher.firstResponseEndTime));
      mServerResponseTime.setValue(net.eiroca.library.core.Helper.elapsed(fetcher.responseStartTime, fetcher.responseEndTime));
      mServerVerified.setValue(1.0);
      mServerResult.setValue(httpStatus);
      mServerStatus.setValue(!ok);
    }
    context.info("Parsing mod_status");
    if (!result.contains("Scoreboard:")) {
      context.info("Error returned page didn't contain the text scoreboard, returned text: ", result);
      CommandException.InfrastructureError("Error returned page didn't contain the text scoreboard.");
    }
    try {
      parseModStatus(result);
    }
    catch (final Exception ex) {
      context.info("Error parsing mod_status error: ", ex.toString());
      CommandException.Invalid("Parsing mod_status response failed");
    }
    return true;
  }

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    super.loadMetricGroup(groups);
    groups.remove(mgProbe);
    groups.add(mgApachePerformance);
    groups.add(mgApacheScoreboard);
  }

  public void parseModStatus(final String theData) {
    // Parse out the timers.
    final StringTokenizer st = new StringTokenizer(theData, "\n");
    while (st.hasMoreTokens()) {
      final String token = st.nextToken();
      final String[] result = token.split(":");
      if (result.length >= 2) {
        result[1] = result[1].replaceAll(",", ".");
        if (result[0].equalsIgnoreCase("Total Accesses")) {
          totalAccesses.setValue(Long.parseLong(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("Total kBytes")) {
          totalkBytes.setValue(Double.parseDouble(result[1].trim()) * 1024);
        }
        else if (result[0].equalsIgnoreCase("CPULoad")) {
          cpuLoad.setValue(Double.parseDouble(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("Uptime")) {
          uptime.setValue(Long.parseLong(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("ReqPerSec")) {
          reqPerSec.setValue(Double.parseDouble(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("BytesPerSec")) {
          bytesPerSec.setValue(Double.parseDouble(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("BytesPerReq")) {
          bytesPerReq.setValue(Double.parseDouble(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("BusyWorkers")) {
          busyWorkers.setValue(Double.parseDouble(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("IdleWorkers")) {
          idleWorkers.setValue(Double.parseDouble(result[1].trim()));
        }
        else if (result[0].equalsIgnoreCase("Scoreboard")) {
          int tWaitingForConnection = 0;
          int tStartingUp = 0;
          int tReadingRequest = 0;
          int tSendingReply = 0;
          int tKeepAlive = 0;
          int tDnsLookup = 0;
          int tClosingConnection = 0;
          int tLogging = 0;
          int tGracefullyFinishing = 0;
          int tIdleCleanupOfWorker = 0;
          int tOpenSlot = 0;
          for (int i = 0; i < result[1].length(); i++) {
            switch (result[1].charAt(i)) {
              case '_':
                tWaitingForConnection++;
                break;
              case 'S':
                tStartingUp++;
                break;
              case 'R':
                tReadingRequest++;
                break;
              case 'W':
                tSendingReply++;
                break;
              case 'K':
                tKeepAlive++;
                break;
              case 'D':
                tDnsLookup++;
                break;
              case 'C':
                tClosingConnection++;
                break;
              case 'L':
                tLogging++;
                break;
              case 'G':
                tGracefullyFinishing++;
                break;
              case 'I':
                tIdleCleanupOfWorker++;
                break;
              case '.':
                tOpenSlot++;
                break;
            }
          }
          sbWaitingForConnection.setValue(tWaitingForConnection);
          sbStartingUp.setValue(tStartingUp);
          sbReadingRequest.setValue(tReadingRequest);
          sbSendingReply.setValue(tSendingReply);
          sbKeepAlive.setValue(tKeepAlive);
          sbDnsLookup.setValue(tDnsLookup);
          sbClosingConnection.setValue(tClosingConnection);
          sbLogging.setValue(tLogging);
          sbGracefullyFinishing.setValue(tGracefullyFinishing);
          sbIdleCleanupOfWorker.setValue(tIdleCleanupOfWorker);
          sbOpenSlot.setValue(tOpenSlot);
        }
      }
    }
    // Calculate how much used the workers are.
    if (idleWorkers.getValue() > 0) {
      workersUtilization.setValue((busyWorkers.getValue() / (busyWorkers.getValue() + idleWorkers.getValue())) * 100);
    }

  } // end read response
}
