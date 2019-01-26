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
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureSplitting;

public class WebServerMonitor extends GenericHTTPMonitor {

  protected static final String CONFIG_PROBECHECK = "checkProbe";
  protected static final String CONFIG_PROBEURL = "probeURL";
  protected static final String CONFIG_PROBEPARSING = "parseProbe";

  MeasureGroup mgHTTPMonitor = new MeasureGroup("HTTP Monitor", "WebServer - {0}");
  Measure smHeaderSize = new Measure(mgHTTPMonitor, "HeaderSize");
  Measure smResponseSize = new Measure(mgHTTPMonitor, "Response Size");
  Measure smResponseThroughput = new Measure(mgHTTPMonitor, "Throughput");
  Measure smHTTPStatusCode = new Measure(mgHTTPMonitor, "HttpStatusCode");
  Measure smConnCloseDelay = new Measure(mgHTTPMonitor, "ConnectionCloseDelay");

  MeasureGroup mgProbe = new MeasureGroup("Query");
  Measure smProbeResult = new Measure(mgProbe, "Result");
  Measure smProbeStatus = new Measure(mgProbe, "Status");
  Measure smProbeRows = new Measure(mgProbe, "Rows");

  protected boolean urlCheck;

  @Override
  public void loadMetricGroup(final List<MeasureGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgHTTPMonitor);
    groups.add(mgProbe);
  }

  @Override
  public ReturnObject fetchResponse() throws CommandException {
    final ReturnObject response = httpCall(fetcher);
    final int httpStatus = fetcher.httpStatusCode;
    smHTTPStatusCode.setValue(httpStatus);
    smHeaderSize.setValue(fetcher.headerSize);
    smConnCloseDelay.setValue(Helper.elapsed(fetcher.connectionCloseStartTime, fetcher.connectionCloseEndTime));
    smResponseSize.setValue(fetcher.responseSize);
    double throughput = 0;
    if ((httpStatus >= 200) && (httpStatus < 300)) {
      final double responseCompleteTimeSecs = Helper.elapsed(fetcher.readResponseStartTime, fetcher.readResponseEndTime) * 0.001;
      if (responseCompleteTimeSecs > 0) {
        final double contentSizeKibiByte = fetcher.responseSize / 1024.0;
        throughput = contentSizeKibiByte / responseCompleteTimeSecs;
      }
    }
    smResponseThroughput.setValue(throughput);
    return response;
  }

  @Override
  public void parseResponse(final ReturnObject response) throws CommandException {
    mServerResult.setValue(response.getRetCode());
    if (context.getConfigBoolean(WebServerMonitor.CONFIG_PROBEPARSING, false)) {
      context.info("Parsing probe data");
      parseProbeData(response.getOutput());
    }
  }

  public void parseProbeData(final String content) {
    boolean success = false;
    String message = null;
    JSONObject obj = null;
    if (content != null) {
      obj = new JSONObject(content);
      success = obj.getBoolean("success");
      message = obj.getString("message");
    }
    smProbeResult.setValue(success ? 1.0 : 0.0);
    if (LibStr.isEmptyOrNull(message)) {
      smProbeStatus.setValue(0.0);
    }
    else {
      smProbeStatus.getSplitting("message").setValue(message, 1.0);
    }
    int max = 0;
    if (obj != null) {
      final JSONArray arr = obj.getJSONArray("infos");
      final MeasureSplitting checkInfo = smProbeResult.getSplitting("check");
      smProbeRows.setValue(arr.length());
      for (int i = 0; i < arr.length(); i++) {
        final JSONObject o = arr.getJSONObject(i);
        final String splitName = o.getString("name");
        final int retCode = o.getInt("retCode");
        if (retCode > max) {
          max = retCode;
        }
        checkInfo.setValue(splitName, retCode);
        context.info(splitName, "=", retCode);
      }
    }
    smProbeResult.setValue(max);
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    boolean ok = super.preCheck(host);
    if (ok) {
      final String method = context.getConfigString(URLFetcherConfig.CONFIG_METHOD, URLFetcherConfig.DEF_METHOD).toUpperCase();
      final String postData = context.getConfigString(URLFetcherConfig.CONFIG_POST_DATA, null);
      fetcher.setURL(getURL(WebServerMonitor.CONFIG_PROBEURL, host.getHostName()));
      fetcher.setMethod(method, postData);
      urlCheck = context.getConfigBoolean(WebServerMonitor.CONFIG_PROBECHECK, true);
      ok = urlCheck;
    }
    return ok;
  }

}
