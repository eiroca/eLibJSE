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
package net.eiroca.library.diagnostics.monitors;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;

public class WebServerMonitor extends GenericHTTPMonitor {

  private static final String SPLIT_CHECK = "check";

  private static final String REGEX_VALUE_FIELD = "value";
  private static final String REGEX_KEY_FIELD = "key";
  private static final String PROBETYPE_JSON = "json";
  private static final String PROBETYPE_REGEX = "regex";

  protected static final String CONFIG_PROBECHECK = "checkProbe";
  protected static final String CONFIG_PROBEURL = "probeURL";
  protected static final String CONFIG_PROBEPARSING = "parseProbe";
  protected static final String CONFIG_PROBETYPE = "probeType";
  protected static final String CONFIG_PROBEREGEX = "probeRegEx";
  private static final String DEF_PROBEREGEX = "\\s*(?<key>\\w+)\\s*\\=\\s*(?<value>[\\.\\deE+-]+)\\s*";
  private static final String DEF_PROBETYPE = WebServerMonitor.PROBETYPE_JSON;

  private boolean config_parseProbe;
  private String config_probeType;
  private Pattern config_probeRegEx;

  MetricGroup mgHTTPMonitor = new MetricGroup(mgMonitor, "WebServer Statistics", "WebServer - {0}");
  Measure smHeaderSize = mgHTTPMonitor.createMeasure("HeaderSize", MetricAggregation.zero, "Size of HTTP headers", "bytes");
  Measure smResponseSize = mgHTTPMonitor.createMeasure("Response Size", MetricAggregation.zero, "Response Size", "bytes");
  Measure smResponseThroughput = mgHTTPMonitor.createMeasure("Throughput", MetricAggregation.zero, "Throughput", "kilobytes", "s");
  Measure smHTTPStatusCode = mgHTTPMonitor.createMeasure("HttpStatusCode", MetricAggregation.zero, "HTTP status code", "number");
  Measure smConnCloseDelay = mgHTTPMonitor.createMeasure("ConnectionCloseDelay", MetricAggregation.zero, "Connection close delay", "ms");

  MetricGroup mgProbe = new MetricGroup(mgMonitor, "Probe Statistics", "Probe - {0}");
  Measure smProbeResult = mgProbe.createMeasure("Result", MetricAggregation.zero, "Probe result", "number").dimensions(WebServerMonitor.SPLIT_CHECK);
  Measure smProbeStatus = mgProbe.createMeasure("Status", MetricAggregation.zero, "Probe status", "number");
  Measure smProbeRows = mgProbe.createMeasure("Rows", MetricAggregation.zero, "Probe number of rows returned", "number");

  protected boolean defaultHasProbe = true;
  protected boolean urlCheck;

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
    boolean success = (content != null);
    if (success) {
      if (config_probeType.equals(WebServerMonitor.PROBETYPE_JSON)) {
        success = parseJson(content);
      }
      else if (config_probeType.equals(WebServerMonitor.PROBETYPE_REGEX)) {
        success = parseRegEx(content);
      }
    }
  }

  private boolean parseRegEx(final String content) {
    final Matcher m = config_probeRegEx.matcher(content);
    final int max = 0;
    int cnt = 0;
    final IMetric<?> checkInfo = smProbeResult.getSplitting(WebServerMonitor.SPLIT_CHECK);
    while (m.find()) {
      cnt++;
      final String key = m.group(WebServerMonitor.REGEX_KEY_FIELD);
      final double value = Helper.getDouble(m.group(WebServerMonitor.REGEX_VALUE_FIELD), 0);
      checkInfo.getSplitting(key).setValue(value);
      context.debug(key, "=", value);
    }
    smProbeRows.setValue(cnt);
    smProbeResult.setValue(max);
    return cnt > 0;
  }

  private boolean parseJson(final String content) {
    boolean success = false;
    String message = null;
    JSONObject obj = null;
    try {
      obj = new JSONObject(content);
      success = obj.getBoolean("success");
      message = obj.getString("message");
    }
    catch (final JSONException e) {
      context.warn("JSON error: " + e.getMessage());
      context.info(e.getMessage() + ": " + content);
    }
    smProbeResult.setValue(success ? 1.0 : 0.0);
    if (LibStr.isEmptyOrNull(message)) {
      smProbeStatus.setValue(0.0);
    }
    else {
      smProbeStatus.getSplitting("message", message).setValue(1.0);
    }
    if (obj != null) {
      int max = 0;
      final JSONArray arr = obj.getJSONArray("infos");
      final IMetric<?> checkInfo = smProbeResult.getSplitting(WebServerMonitor.SPLIT_CHECK);
      smProbeRows.setValue(arr.length());
      for (int i = 0; i < arr.length(); i++) {
        final JSONObject o = arr.getJSONObject(i);
        final String splitName = o.getString("name");
        final int retCode = o.getInt("retCode");
        if (retCode > max) {
          max = retCode;
        }
        checkInfo.getSplitting(splitName).setValue(retCode);
        context.debug(splitName, "=", retCode);
      }
      smProbeResult.setValue(max);
    }
    return success;
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    final boolean ok = super.preCheck(host);
    if (ok) {
      urlCheck = context.getConfigBoolean(WebServerMonitor.CONFIG_PROBECHECK, defaultHasProbe);
      if (urlCheck) {
        final String method = context.getConfigString(URLFetcherConfig.CONFIG_METHOD, URLFetcherConfig.DEF_METHOD).toUpperCase();
        final String postData = context.getConfigString(URLFetcherConfig.CONFIG_POST_DATA, null);
        fetcher.setURL(getURL(WebServerMonitor.CONFIG_PROBEURL, host.getHostName()));
        fetcher.setMethod(method, postData);
        config_parseProbe = context.getConfigBoolean(WebServerMonitor.CONFIG_PROBEPARSING, false);
        if (config_parseProbe) {
          config_probeType = context.getConfigString(WebServerMonitor.CONFIG_PROBETYPE, WebServerMonitor.DEF_PROBETYPE).toLowerCase();
          config_probeRegEx = Pattern.compile(context.getConfigString(WebServerMonitor.CONFIG_PROBEREGEX, WebServerMonitor.DEF_PROBEREGEX));
        }
      }
    }
    return ok;
  }

}
