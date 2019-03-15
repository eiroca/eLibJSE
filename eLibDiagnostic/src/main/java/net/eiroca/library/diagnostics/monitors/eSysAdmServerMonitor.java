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
import net.eiroca.library.core.LibMath;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.ILog.LogLevel;

public class eSysAdmServerMonitor extends RESTServerMonitor {

  private static final String CONFIG_PORT = "port";
  private static final String CONFIG_NAMESPACE = "namespace";
  private static final String CONFIG_AGGREGATION_MEASURE = "aggregation";
  private static final String CONFIG_AGGREGATION_SPLIT = "splitting_aggregation";

  // measurement variables
  protected final MetricGroup mgeSysAdm = new MetricGroup("eSysAdm Monitor", "eSysAdm - {0}");
  protected final Measure mMetrics = mgeSysAdm.createMeasure("Metrics", "Metrics collected by eSysAdm server", "number");
  protected final Measure mAlerts = mgeSysAdm.createMeasure("Alerts", "Alerts collected by eSysAdm server", "number");
  protected final Measure mKPIs = mgeSysAdm.createMeasure("KPIs", "KPIs (%) collected by eSysAdm server", "percent");
  protected final Measure mTimings = mgeSysAdm.createMeasure("Timings", "Timings (ms) collected by eSysAdm server", "ms");

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
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
    if (data == null) { return null; }
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
          val = LibMath.average(data.getLong("count"), data.getDouble("sumX"));
          break;
        case stddev:
          val = LibMath.stddev(data.getLong("count"), data.getDouble("sumX"), data.getDouble("sumX2"));
          break;
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
      JSONObject value = data.optJSONObject("value");
      final Double val = getValue(value, measureAggregation);
      if (val != null) {
        m.setValue(val);
      }
      context.info("M: ", m);
      if (splittings != null) {
        final Iterator<String> splittingsGroups = splittings.keys();
        while (splittingsGroups.hasNext()) {
          final String splitGroup = splittingsGroups.next();
          data = splittings.getJSONObject(splitGroup);
          final IMetric<?> ms = m.getSplitting(splitGroup);
          final JSONObject splittingSubKeysObj = data.optJSONObject("splittings");
          final Iterator<String> splittingsSubKeys = splittingSubKeysObj.keys();
          while (splittingsSubKeys.hasNext()) {
            final String splitName = splittingsSubKeys.next();
            data = splittingSubKeysObj.getJSONObject(splitName);
            value = data.optJSONObject("value");
            final Double splitVal = getValue(value, splitAggregation);
            if (splitVal != null) {
              context.logF(LogLevel.info, "{0}.{1}={2}", splitGroup, splitName, splitVal);
              final IMetric<?> mm = ms.getSplitting(splitName);
              mm.setValue(splitVal);
            }
          }
        }
      }
    }
  }
}
