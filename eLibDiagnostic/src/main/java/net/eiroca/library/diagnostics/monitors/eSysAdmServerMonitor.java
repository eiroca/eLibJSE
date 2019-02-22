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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.system.ILog.LogLevel;

public class eSysAdmServerMonitor extends RESTServerMonitor {

  private static final String CONFIG_PORT = "port";
  private static final String CONFIG_NAMESPACE = "namespace";
  private static final String CONFIG_AGGREGATION_MEASURE = "aggregation";
  private static final String CONFIG_AGGREGATION_SPLIT = "splitting_aggregation";

  enum MetricAggregation {
    average, min, max, first, last, count, sum
  }

  // measurement variables
  public MeasureGroup mgeSysAdm = new MeasureGroup("eSysAdm Monitor", "eSysAdm - {0}");
  public Measure mMetrics = mgeSysAdm.createMeasure("Metrics");
  public Measure mAlerts = mgeSysAdm.createMeasure("Alerts");
  public Measure mKPIs = mgeSysAdm.createMeasure("KPIs");
  public Measure mTimings = mgeSysAdm.createMeasure("Timings");

  @Override
  public void loadMetricGroup(final List<MeasureGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgeSysAdm);
  }

  @Override
  public URL getURL(final InetAddress host) throws MalformedURLException {
    final String port = "" + context.getConfigInt(eSysAdmServerMonitor.CONFIG_PORT, 2000);
    final String namespace = context.getConfigString(eSysAdmServerMonitor.CONFIG_NAMESPACE, "ubknowmn");
    final String urlStr = MessageFormat.format("http://{0}:{1}/rest/export/{2}", host.getHostName(), port, namespace);
    context.info("URL: ", urlStr);
    return new URL(urlStr);
  }

  public Double getValue(final JSONObject data, final MetricAggregation aggregation) {
    double val = 0;
    boolean ok = true;
    try {
      switch (aggregation) {
        case min:
          val = data.getDouble("min");
          break;
        case max:
          val = data.getDouble("max");
          break;
        case first:
          val = data.getDouble("fisrt");
          break;
        case last:
          val = data.getDouble("last");
          break;
        case sum:
          val = data.getDouble("sumX");
          break;
        case count:
          val = data.getDouble("count");
          break;
        case average:
          final long cnt = data.getLong("count");
          if (cnt > 0) {
            val = data.getDouble("sumX") / cnt;
          }
          else {
            ok = false;
          }
      }
    }
    catch (final JSONException e) {
      ok = false;
    }
    return ok ? val : null;
  }

  @Override
  public void parseJSON(final JSONObject obj) {
    final String measureAggregationStr = context.getConfigString(eSysAdmServerMonitor.CONFIG_AGGREGATION_MEASURE, MetricAggregation.average.toString());
    final String aplitAggregationStr = context.getConfigString(eSysAdmServerMonitor.CONFIG_AGGREGATION_SPLIT, MetricAggregation.average.toString());
    MetricAggregation measureAggregation = MetricAggregation.average;
    MetricAggregation splitAggregation = MetricAggregation.last;
    try {
      measureAggregation = MetricAggregation.valueOf(measureAggregationStr);
    }
    catch (final IllegalArgumentException e) {
    }
    try {
      splitAggregation = MetricAggregation.valueOf(aplitAggregationStr);
    }
    catch (final IllegalArgumentException e) {
    }
    int status;
    String message = null;
    try {
      status = obj.getInt("status");
      message = obj.optString("message", "");
    }
    catch (final JSONException e) {
      message = net.eiroca.library.core.Helper.getExceptionAsString(e, false);
      status = 1;
    }
    context.logF(LogLevel.info, "status: {0} message: {1}", status, message);
    mServerStatus.setValue(status);
    if (status != 0) { return; }
    final JSONObject result = obj.getJSONObject("result");
    context.info("result:", result);
    final Iterator<String> metrics = result.keys();
    Measure m = null;
    while (metrics.hasNext()) {
      final String metric = metrics.next();
      if (metric.equalsIgnoreCase("result")) {
        m = mServerResult;
      }
      else if (metric.equalsIgnoreCase("alerts") || metric.equalsIgnoreCase("alert")) {
        m = mAlerts;
      }
      else if (metric.equalsIgnoreCase("metrics") || metric.equalsIgnoreCase("metric")) {
        m = mMetrics;
      }
      else if (metric.equalsIgnoreCase("kpis") || metric.equalsIgnoreCase("kpi")) {
        m = mKPIs;
      }
      else if (metric.equalsIgnoreCase("timings") || metric.equalsIgnoreCase("timing")) {
        m = mTimings;
      }
      else {
        m = null;
      }
      if (m == null) {
        continue;
      }
      JSONObject data = result.getJSONObject(metric);
      final JSONObject splittings = data.optJSONObject("splittings");
      final Double val = getValue(data, measureAggregation);
      if (val != null) {
        m.setValue(val);
      }
      context.info("M: ", m);
      if (splittings != null) {
        final Iterator<String> splittingsKeys = splittings.keys();
        while (splittingsKeys.hasNext()) {
          final String splittingKey = splittingsKeys.next();
          data = splittings.getJSONObject(splittingKey);
          final MeasureSplitting ms = m.getSplitting(splittingKey);
          final JSONObject splittingSubKeysObj = data.optJSONObject("splittings");
          final Iterator<String> splittingsSubKeys = splittingSubKeysObj.keys();
          while (splittingsSubKeys.hasNext()) {
            final String splittingsSubKey = splittingsSubKeys.next();
            data = splittingSubKeysObj.getJSONObject(splittingsSubKey);
            final Double splitVal = getValue(data, splitAggregation);
            if (splitVal != null) {
              context.logF(LogLevel.info, "{0}.{1}={2}", splittingKey, splittingsSubKey, splitVal);
              ms.setValue(splittingsSubKey, splitVal);
            }
          }
        }
      }
    }
  }
}
