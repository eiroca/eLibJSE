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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.library.config.parameter.BooleanParameter;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.data.Tags;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.LibFile;
import net.eiroca.library.system.LibRegEx;

public class GraphiteMonitor extends GenericHTTPMonitor {

  // measurement variables
  protected final MetricGroup mgGraphite = new MetricGroup("Graphite Monitor", "Graphite - {0}");
  protected final Measure mAgents_activeConnections = mgGraphite.createMeasure("Agents - activeConnections", "Agents - activeConnections", "num");
  protected final Measure mAgents_avgUpdateTime = mgGraphite.createMeasure("Agents - avgUpdateTime", "Agents - avgUpdateTime", "ms");
  protected final Measure mAgents_blacklistMatches = mgGraphite.createMeasure("Agents - blacklistMatches", "Agents - blacklistMatches", "num");
  protected final Measure mAgents_cache_bulk_queries = mgGraphite.createMeasure("Agents - cache.bulk_queries", "Agents - cache.bulk_queries", "num");
  protected final Measure mAgents_cache_overflow = mgGraphite.createMeasure("Agents - cache.overflow", "Agents - cache.overflow", "num");
  protected final Measure mAgents_cache_queries = mgGraphite.createMeasure("Agents - cache.queries", "Agents - cache.queries", "num");
  protected final Measure mAgents_cache_queues = mgGraphite.createMeasure("Agents - cache.queues", "Agents - cache.queues", "num");
  protected final Measure mAgents_cache_size = mgGraphite.createMeasure("Agents - cache.size", "Agents - cache.size", "num");
  protected final Measure mAgents_committedPoints = mgGraphite.createMeasure("Agents - committedPoints", "Agents - committedPoints", "num");
  protected final Measure mAgents_cpuUsage = mgGraphite.createMeasure("Agents - cpuUsage", "Agents - cpuUsage", "num");
  protected final Measure mAgents_creates = mgGraphite.createMeasure("Agents - creates", "Agents - creates", "num");
  protected final Measure mAgents_droppedCreates = mgGraphite.createMeasure("Agents - droppedCreates", "Agents - droppedCreates", "num");
  protected final Measure mAgents_errors = mgGraphite.createMeasure("Agents - errors", "Agents - errors", "num");
  protected final Measure mAgents_memUsage = mgGraphite.createMeasure("Agents - memUsage", "Agents - memUsage", "num");
  protected final Measure mAgents_metricsReceived = mgGraphite.createMeasure("Agents - metricsReceived", "Agents - metricsReceived", "num");
  protected final Measure mAgents_pointsPerUpdate = mgGraphite.createMeasure("Agents - pointsPerUpdate", "Agents - pointsPerUpdate", "num");
  protected final Measure mAgents_updateOperations = mgGraphite.createMeasure("Agents - updateOperations", "Agents - updateOperations", "num");
  protected final Measure mAgents_whitelistRejects = mgGraphite.createMeasure("Agents - whitelistRejects", "Agents - whitelistRejects", "num");
  protected final Measure mAggregator_activeConnections = mgGraphite.createMeasure("Aggregator - activeConnections", "Aggregator - activeConnections", "num");
  protected final Measure mAggregator_aggregateDatapointsSent = mgGraphite.createMeasure("Aggregator - aggregateDatapointsSent", "Aggregator - aggregateDatapointsSent", "num");
  protected final Measure mAggregator_allocatedBuffers = mgGraphite.createMeasure("Aggregator - allocatedBuffers", "Aggregator - allocatedBuffers", "num");
  protected final Measure mAggregator_blacklistMatches = mgGraphite.createMeasure("Aggregator - blacklistMatches", "Aggregator - blacklistMatches", "num");
  protected final Measure mAggregator_bufferedDatapoints = mgGraphite.createMeasure("Aggregator - bufferedDatapoints", "Aggregator - bufferedDatapoints", "num");
  protected final Measure mAggregator_cpuUsage = mgGraphite.createMeasure("Aggregator - cpuUsage", "Aggregator - cpuUsage", "num");
  protected final Measure mAggregator_destinations_attemptedRelays = mgGraphite.createMeasure("Aggregator - destinations - attemptedRelays", "Aggregator - destinations - attemptedRelays", "num");
  protected final Measure mAggregator_destinations_batchesSent = mgGraphite.createMeasure("Aggregator - destinations - batchesSent", "Aggregator - destinations - batchesSent", "num");
  protected final Measure mAggregator_destinations_relayMaxQueueLength = mgGraphite.createMeasure("Aggregator - destinations - relayMaxQueueLength", "Aggregator - destinations - relayMaxQueueLength", "num");
  protected final Measure mAggregator_destinations_sent = mgGraphite.createMeasure("Aggregator - destinations - sent", "Aggregator - destinations - sent", "num");
  protected final Measure mAggregator_memUsage = mgGraphite.createMeasure("Aggregator - memUsage", "Aggregator - memUsage", "num");
  protected final Measure mAggregator_metricsReceived = mgGraphite.createMeasure("Aggregator - metricsReceived", "Aggregator - metricsReceived", "num");
  protected final Measure mAggregator_whitelistRejects = mgGraphite.createMeasure("Aggregator - whitelistRejects", "Aggregator - whitelistRejects", "num");
  protected final Measure mStats_deamom_calculationtime = mgGraphite.createMeasure("Stats - deamom - calculationtime", "Stats - deamom - calculationtime", "num");
  protected final Measure mStats_deamom_flush_length = mgGraphite.createMeasure("Stats - deamom - flush_length", "Stats - deamom - flush_length", "num");
  protected final Measure mStats_deamom_flush_time = mgGraphite.createMeasure("Stats - deamom - flush_time", "Stats - deamom - flush_time", "num");
  protected final Measure mStats_deamom_last_exception = mgGraphite.createMeasure("Stats - deamom - last_exception", "Stats - deamom - last_exception", "num");
  protected final Measure mStats_deamom_last_flush = mgGraphite.createMeasure("Stats - deamom - last_flush", "Stats - deamom - last_flush", "num");
  protected final Measure mStats_deamom_processing_time = mgGraphite.createMeasure("Stats - deamom - processing_time", "Stats - deamom - processing_time", "num");
  protected final Measure mStats_numStats = mgGraphite.createMeasure("Stats - numStats", "Stats - numStats", "num");
  protected final Measure mStats_timestamp_lag = mgGraphite.createMeasure("Stats - timestamp_lag", "Stats - timestamp_lag", "num");
  //
  protected final Measure mStats_response = mgGraphite.createMeasure("Stats - response", "Stats - response", "ms");
  protected final Measure mStats_deamom_bad_lines_seen = mgGraphite.createMeasure("Stats - deamom - bad_lines_seen", "Stats - deamom - bad_lines_seen", "num");
  protected final Measure mStats_deamom_metrics_received = mgGraphite.createMeasure("Stats - deamom - metrics_received", "Stats - deamom - metrics_received", "num");
  protected final Measure mStats_deamom_packets_received = mgGraphite.createMeasure("Stats - deamom - packets_received", "Stats - deamom - packets_received", "num");
  protected final Measure mStatsCounts_response = mgGraphite.createMeasure("Stats_counts - response", "Stats_counts - response", "ms");
  protected final Measure mStatsCounts_deamom_bad_lines_seen = mgGraphite.createMeasure("Stats_counts - deamom - bad_lines_seen", "Stats_counts - deamom - bad_lines_seen", "num");
  protected final Measure mStatsCounts_deamom_metrics_received = mgGraphite.createMeasure("Stats_counts - deamom - metrics_received", "Stats_counts - deamom - metrics_received", "num");
  protected final Measure mStatsCounts_deamom_packets_received = mgGraphite.createMeasure("Stats_counts - deamom - packets_received", "Stats_counts - deamom - packets_received", "num");
  //
  protected final Measure mStats_timers_count = mgGraphite.createMeasure("Stats - timers - count", "Stats - timers - count", "num");
  protected final Measure mStats_timers_count_90 = mgGraphite.createMeasure("Stats - timers - count_90", "Stats - timers - count_90", "num");
  protected final Measure mStats_timers_count_ps = mgGraphite.createMeasure("Stats - timers - count_ps", "Stats - timers - count_ps", "num");
  protected final Measure mStats_timers_lower = mgGraphite.createMeasure("Stats - timers - lower", "Stats - timers - lower", "ms");
  protected final Measure mStats_timers_lower_90 = mgGraphite.createMeasure("Stats - timers - lower_90", "Stats - timers - lower_90", "ms");
  protected final Measure mStats_timers_lower_ps = mgGraphite.createMeasure("Stats - timers - lower_ps", "Stats - timers - lower_ps", "ms");
  protected final Measure mStats_timers_mean = mgGraphite.createMeasure("Stats - timers - mean", "Stats - timers - mean", "ms");
  protected final Measure mStats_timers_mean_90 = mgGraphite.createMeasure("Stats - timers - mean_90", "Stats - timers - mean_90", "ms");
  protected final Measure mStats_timers_mean_ps = mgGraphite.createMeasure("Stats - timers - mean_ps", "Stats - timers - mean_ps", "ms");
  protected final Measure mStats_timers_median = mgGraphite.createMeasure("Stats - timers - median", "Stats - timers - median", "ms");
  protected final Measure mStats_timers_std = mgGraphite.createMeasure("Stats - timers - std", "Stats - timers - std", "ms");
  protected final Measure mStats_timers_sum = mgGraphite.createMeasure("Stats - timers - sum", "Stats - timers - sum", "ms");
  protected final Measure mStats_timers_sum_90 = mgGraphite.createMeasure("Stats - timers - sum_90", "Stats - timers - sum_90", "ms");
  protected final Measure mStats_timers_sum_ps = mgGraphite.createMeasure("Stats - timers - sum_ps", "Stats - timers - sum_ps", "ms");
  protected final Measure mStats_timers_sum_squares = mgGraphite.createMeasure("Stats - timers - sum_squares", "Stats - timers - sum_squares", "num");
  protected final Measure mStats_timers_sum_squares_90 = mgGraphite.createMeasure("Stats - timers - sum_squares_90", "Stats - timers - sum_squares_90", "num");
  protected final Measure mStats_timers_sum_squares_ps = mgGraphite.createMeasure("Stats - timers - sum_squares_ps", "Stats - timers - sum_squares_ps", "num");
  protected final Measure mStats_timers_upper = mgGraphite.createMeasure("Stats - timers - upper", "Stats - timers - upper", "ms");
  protected final Measure mStats_timers_upper_90 = mgGraphite.createMeasure("Stats - timers - upper_90", "Stats - timers - upper_90", "ms");
  protected final Measure mStats_timers_upper_ps = mgGraphite.createMeasure("Stats - timers - upper_ps", "Stats - timers - upper_ps", "ms");
  //
  protected final Measure mOthers_Stats = mgGraphite.createMeasure("Others - Stats", "Others - Stats", "num");
  // Configuration
  protected static StringParameter _graphiteURL = new StringParameter(ServerMonitor.params, "graphiteURL", "http://{host}");
  protected static BooleanParameter _allMetrics = new BooleanParameter(ServerMonitor.params, "allMetrics", false);
  protected static BooleanParameter _systemMetrics = new BooleanParameter(ServerMonitor.params, "systemMetrics", true);
  protected static BooleanParameter _customMetrics = new BooleanParameter(ServerMonitor.params, "customMetrics", true);
  protected static StringParameter _target = new StringParameter(ServerMonitor.params, "target", "");
  protected static StringParameter _mapping = new StringParameter(ServerMonitor.params, "mapping", "");
  protected static StringParameter _from = new StringParameter(ServerMonitor.params, "from", "-15min");
  protected static StringParameter _until = new StringParameter(ServerMonitor.params, "until", null);
  protected static IntegerParameter _groupLevel = new IntegerParameter(ServerMonitor.params, "groupLevel", -1);
  protected static StringParameter _tagPrefix = new StringParameter(ServerMonitor.params, "tagPrefix", "graphite.");
  //
  protected String config_graphiteURL;
  protected boolean config_allMetrics;
  protected boolean config_systemMetrics;
  protected boolean config_customMetrics;
  protected String config_target;
  protected String config_mapping;
  protected String config_from;
  protected String config_until;
  protected int config_groupLevel;
  protected String config_tagPrefix;
  //
  protected String baseURL;

  private final class RuleEntry {

    Pattern regex;
    IMetric<?> metric;
    String splitGroup;
    String splitName;
  }

  private final List<String> systemMetrics = new ArrayList<>();
  private final List<String> customMetrics = new ArrayList<>();

  private final List<RuleEntry> systemRules = new ArrayList<>();
  private final List<RuleEntry> customRules = new ArrayList<>();

  public GraphiteMonitor() throws CommandException {
    super();
    decodeMetrics(LibFile.readFile("graphite/system_metrics.txt"), systemMetrics);
    decodeRules(LibFile.readFile("graphite/system_rules.csv"), systemRules, true);
  }

  private void decodeRules(String rules, List<RuleEntry> result, boolean isSystemRule) throws CommandException {
    int base = isSystemRule ? 2 : 1;
    int cnt = isSystemRule ? 4 : 3;
    for (String x : LibStr.splitNL(rules, (char)0)) {
      if (LibStr.isEmptyOrNull(x)) continue;
      List<String> r = LibStr.split(x.trim(), ';', '"');
      if (r.size() != cnt) continue;
      RuleEntry e = new RuleEntry();
      e.regex = Pattern.compile(r.get(0));
      if (isSystemRule) {
        e.metric = mgGraphite.find(r.get(1), false);
      }
      else e.metric = mServerResult;
      e.splitGroup = r.get(base + 0);
      e.splitName = r.get(base + 1);
      if (e.metric != null) {
        result.add(e);
      }
      else {
        CommandException.Invalid("Invalid rule: " + x);
      }
    }
  }

  private void decodeMetrics(String metrics, List<String> result) {
    for (String x : LibStr.splitNL(metrics, (char)0)) {
      if (LibStr.isEmptyOrNull(x)) continue;
      result.add(x.trim());
    }
  }

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgGraphite);
  }

  @Override
  public void readConf() throws CommandException {
    super.readConf();
    fetcher.setMethod("GET", null);
    ServerMonitor.params.convert(context, null, this, "config_");
    if (LibStr.isEmptyOrNull(config_from)) {
      config_from = null;
    }
    if (LibStr.isEmptyOrNull(config_until)) {
      config_until = null;
    }
    customMetrics.clear();
    decodeMetrics(config_target, customMetrics);
    customRules.clear();
    decodeRules(config_mapping, customRules, false);
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    final boolean ok = super.preCheck(host);
    if (ok) {
      baseURL = formatURL(config_graphiteURL, host.getHostName());
      context.info("Graphite URL: " + baseURL);
      fetcher.setMethod(URLFetcherConfig.METHOD_GET, null);
    }
    return ok;
  }

  @Override
  public boolean runCheck() throws CommandException {
    final List<String> targets = getTargets();
    final List<RuleEntry> rules = getRules();
    if (targets != null) {
      for (final String target : targets) {
        context.info("" + target);
        final StringBuilder sb = new StringBuilder(baseURL).append("/render");
        boolean first = true;
        first = HttpClientHelper.appendParam(sb, "target", target, first);
        first = HttpClientHelper.appendParam(sb, "from", config_from, first);
        first = HttpClientHelper.appendParam(sb, "until", config_until, first);
        first = HttpClientHelper.appendParam(sb, "format", "json", first);
        if (fetcher.setURL(sb.toString())) {
          final ReturnObject response = httpCall(fetcher);
          validateResponse(response, validator);
          if (response.getRetCode() < 400) {
            String json = response.getOutput();
            context.info("JSON: " + json);
            try {
              final JSONArray obj = new JSONArray(json);
              parseJSON(obj, rules);
            }
            catch (final Exception ex) {
              context.warn("Error parsing JSON: ", json);
              context.debug("Error: ", ex.toString());
              CommandException.Invalid("Parsing JSON response failed");
            }
          }
          else {
            context.warn("Invalid response from graphite: ", sb.toString(), " -> ", response.getRetCode());
            context.debug(sb.toString(), " -> ", response.getOutput());
          }
        }
        else {
          CommandException.Invalid("Invalid URL " + sb.toString());
        }
      }
    }
    return true;
  }

  private List<RuleEntry> getRules() {
    List<RuleEntry> result = new ArrayList<>();
    if (config_customMetrics) result.addAll(customRules);
    result.addAll(systemRules);
    return result;
  }

  private List<String> getTargets() {
    if (config_allMetrics) return getIndex();
    List<String> result = new ArrayList<>();
    if (config_systemMetrics) result.addAll(systemMetrics);
    if (config_customMetrics) result.addAll(customMetrics);
    return result;
  }

  private List<String> getIndex() {
    fetcher.setURL(baseURL + "/metrics/index.json");
    final ReturnObject response = httpCall(fetcher);
    if (response.getRetCode() < 400) { return GraphiteMonitor.toList(new JSONArray(response.getOutput())); }
    return null;
  }

  private void parseJSON(final JSONArray obj, List<RuleEntry> rules) {
    for (int i = 0; i < obj.length(); i++) {
      final JSONObject o = obj.getJSONObject(i);
      final String target = o.getString("target");
      final JSONArray datapoints = o.getJSONArray("datapoints");
      final JSONObject tags = o.optJSONObject("tags");
      if (datapoints.length() > 0) {
        processTarget(target, rules, datapoints, tags);
      }
    }
  }

  private void processTarget(final String target, List<RuleEntry> rules, final JSONArray datapoints, JSONObject tags) {
    double v = 0;
    long t = -1;
    for (int i = 0; i < datapoints.length(); i++) {
      final JSONArray entry = datapoints.getJSONArray(i);
      final Object val = entry.get(0);
      if ((val == null) || ("null".equals(val.toString()))) {
        continue;
      }
      v = Helper.getDouble(val.toString(), 0.0);
      t = Helper.getLong(String.valueOf(entry.get(1)), 0);
    }
    if (t > 0) {
      processEntry(target, rules, v, t, tags);
    }
  }

  private void processEntry(final String target, List<RuleEntry> rules, final double v, final long t, JSONObject tags) {
    IMetric<?> m = mServerResult;
    String g = "metrics";
    String s = target;
    final RuleEntry e = findEntry(target, rules);
    if (e != null) {
      m = e.metric;
      if (m == null) {
        m = mServerResult;
      }
      g = e.splitGroup;
      s = e.splitName;
    }
    else {
      int dotCount = 0;
      int lastDot = -1;
      for (int i = 0; i < target.length(); i++) {
        final char ch = target.charAt(i);
        if (ch == '.') {
          dotCount++;
          lastDot = i;
        }
        if (dotCount == config_groupLevel) {
          g = target.substring(0, i);
          s = target.substring(i + 1);
          lastDot = -1;
          break;
        }
      }
      if (lastDot >= 0) {
        g = target.substring(0, lastDot);
        s = target.substring(lastDot + 1);
      }
    }
    context.debug(m.getMetadata().getDisplayName() + "#" + g + "[" + s + "]->" + v);
    m.getSplitting(g, s).addValue(v);
    if (tags != null) addTags(m, tags);
  }

  private void addTags(IMetric<?> m, JSONObject tags) {
    Tags metricTag = m.getTags();
    for (String tagName : tags.keySet()) {
      String tagVal = tags.getString(tagName);
      metricTag.add(LibStr.isEmptyOrNull(config_tagPrefix) ? tagName : config_tagPrefix + tagName, tagVal);
    }
  }

  private RuleEntry findEntry(final String target, List<RuleEntry> rules) {
    for (final RuleEntry e : rules) {
      final Pattern rule = e.regex;
      final Matcher m = rule.matcher(target);
      if (m.matches()) {
        final RuleEntry r = new RuleEntry();
        r.metric = e.metric;
        r.splitGroup = LibRegEx.expand(e.splitGroup, m);
        r.splitName = LibRegEx.expand(e.splitName, m);
        return r;
      }
    }
    return null;
  }

  public static List<String> toList(final JSONArray data) {
    if (data == null) { return null; }
    final List<String> result = new ArrayList<>();
    for (int i = 0; i < data.length(); i++) {
      result.add(data.getString(i));
    }
    return result;
  }

}
