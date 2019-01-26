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

import java.util.List;
import java.util.StringTokenizer;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.ext.library.http.utils.URLFetcherException;
import net.eiroca.library.diagnostics.CommandError;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.SimpleMeasure;

public class ApacheServerMonitor extends WebServerMonitor {

  private static final String CONFIG_MODSTATUSURL = "modStatusURL";

  // measurement variables
  public MeasureGroup mgApachePerformance = new MeasureGroup("Apache Performance", "Apache - {0}");
  public SimpleMeasure totalAccesses = new Measure(mgApachePerformance, "TotalAccesses");
  public SimpleMeasure totalkBytes = new Measure(mgApachePerformance, "TotalBytes");
  public SimpleMeasure cpuLoad = new Measure(mgApachePerformance, "CPULoad");
  public SimpleMeasure uptime = new Measure(mgApachePerformance, "Uptime");
  public SimpleMeasure reqPerSec = new Measure(mgApachePerformance, "ReqPerSec");
  public SimpleMeasure bytesPerSec = new Measure(mgApachePerformance, "BytesPerSec");
  public SimpleMeasure bytesPerReq = new Measure(mgApachePerformance, "BytesPerReq");
  public SimpleMeasure busyWorkers = new Measure(mgApachePerformance, "BusyWorkers");
  public SimpleMeasure idleWorkers = new Measure(mgApachePerformance, "IdleWorkers");
  public SimpleMeasure workersUtilization = new Measure(mgApachePerformance, "WorkersUtilization");

  public MeasureGroup mgApacheScoreboard = new MeasureGroup("Apache Scoreboard", "Apache - Scoreboard - {0}");
  public SimpleMeasure sbWaitingForConnection = new Measure(mgApacheScoreboard, "Waiting");
  public SimpleMeasure sbStartingUp = new Measure(mgApacheScoreboard, "Starting up");
  public SimpleMeasure sbReadingRequest = new Measure(mgApacheScoreboard, "Reading Request");
  public SimpleMeasure sbSendingReply = new Measure(mgApacheScoreboard, "Sending Reply");
  public SimpleMeasure sbKeepAlive = new Measure(mgApacheScoreboard, "Keepalive (read)");
  public SimpleMeasure sbDnsLookup = new Measure(mgApacheScoreboard, "DNS Lookup");
  public SimpleMeasure sbClosingConnection = new Measure(mgApacheScoreboard, "Closing connection");
  public SimpleMeasure sbLogging = new Measure(mgApacheScoreboard, "Logging");
  public SimpleMeasure sbGracefullyFinishing = new Measure(mgApacheScoreboard, "Gracefully finishing");
  public SimpleMeasure sbIdleCleanupOfWorker = new Measure(mgApacheScoreboard, "Idle cleanup of worker");
  public SimpleMeasure sbOpenSlot = new Measure(mgApacheScoreboard, "Open Slot");

  @Override
  public boolean runCheck() throws CommandException {
    if (urlCheck) {
      super.runCheck();
    }
    fetcher.setURL(getURL(ApacheServerMonitor.CONFIG_MODSTATUSURL, targetHost.getHostString()));
    fetcher.setMethod(URLFetcherConfig.METHOD_GET, null);
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
  public void loadMetricGroup(final List<MeasureGroup> groups) {
    super.loadMetricGroup(groups);
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
