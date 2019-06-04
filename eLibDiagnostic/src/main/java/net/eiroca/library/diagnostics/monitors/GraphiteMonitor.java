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
import net.eiroca.library.core.LibParser;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.data.Tags;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.regex.LibRegEx;
import net.eiroca.library.system.LibFile;

public class GraphiteMonitor extends GenericHTTPMonitor {

  // measurement variables
  protected final MetricGroup mgGraphite = new MetricGroup(mgMonitor, "Graphite Statistics", "Graphite - {0}");
  //
  protected final MetricGroup mgGraphiteAgents = new MetricGroup(mgGraphite, "Graphite Agents", "Graphite - Agents - {0}");
  protected final Measure mAgents_activeConnections = mgGraphiteAgents.createMeasure("activeConnections", "Agents - activeConnections", "num");
  protected final Measure mAgents_avgUpdateTime = mgGraphiteAgents.createMeasure("avgUpdateTime", "Agents - avgUpdateTime", "ms");
  protected final Measure mAgents_blacklistMatches = mgGraphiteAgents.createMeasure("blacklistMatches", "Agents - blacklistMatches", "num");
  protected final Measure mAgents_cache_bulk_queries = mgGraphiteAgents.createMeasure("cache.bulk_queries", "Agents - cache.bulk_queries", "num");
  protected final Measure mAgents_cache_overflow = mgGraphiteAgents.createMeasure("cache.overflow", "Agents - cache.overflow", "num");
  protected final Measure mAgents_cache_queries = mgGraphiteAgents.createMeasure("cache.queries", "Agents - cache.queries", "num");
  protected final Measure mAgents_cache_queues = mgGraphiteAgents.createMeasure("cache.queues", "Agents - cache.queues", "num");
  protected final Measure mAgents_cache_size = mgGraphiteAgents.createMeasure("cache.size", "Agents - cache.size", "num");
  protected final Measure mAgents_committedPoints = mgGraphiteAgents.createMeasure("committedPoints", "Agents - committedPoints", "num");
  protected final Measure mAgents_cpuUsage = mgGraphiteAgents.createMeasure("cpuUsage", "Agents - cpuUsage", "num");
  protected final Measure mAgents_creates = mgGraphiteAgents.createMeasure("creates", "Agents - creates", "num");
  protected final Measure mAgents_droppedCreates = mgGraphiteAgents.createMeasure("droppedCreates", "Agents - droppedCreates", "num");
  protected final Measure mAgents_errors = mgGraphiteAgents.createMeasure("errors", "Agents - errors", "num");
  protected final Measure mAgents_memUsage = mgGraphiteAgents.createMeasure("memUsage", "Agents - memUsage", "num");
  protected final Measure mAgents_metricsReceived = mgGraphiteAgents.createMeasure("metricsReceived", "Agents - metricsReceived", "num");
  protected final Measure mAgents_pointsPerUpdate = mgGraphiteAgents.createMeasure("pointsPerUpdate", "Agents - pointsPerUpdate", "num");
  protected final Measure mAgents_updateOperations = mgGraphiteAgents.createMeasure("updateOperations", "Agents - updateOperations", "num");
  protected final Measure mAgents_whitelistRejects = mgGraphiteAgents.createMeasure("whitelistRejects", "Agents - whitelistRejects", "num");
  //
  protected final MetricGroup mgGraphiteAggregator = new MetricGroup(mgGraphite, "Graphite Aggregator", "Graphite - Aggregator - {0}");
  protected final Measure mAggregator_activeConnections = mgGraphiteAggregator.createMeasure("activeConnections", "Aggregator - activeConnections", "num");
  protected final Measure mAggregator_aggregateDatapointsSent = mgGraphiteAggregator.createMeasure("aggregateDatapointsSent", "Aggregator - aggregateDatapointsSent", "num");
  protected final Measure mAggregator_allocatedBuffers = mgGraphiteAggregator.createMeasure("allocatedBuffers", "Aggregator - allocatedBuffers", "num");
  protected final Measure mAggregator_blacklistMatches = mgGraphiteAggregator.createMeasure("blacklistMatches", "Aggregator - blacklistMatches", "num");
  protected final Measure mAggregator_bufferedDatapoints = mgGraphiteAggregator.createMeasure("bufferedDatapoints", "Aggregator - bufferedDatapoints", "num");
  protected final Measure mAggregator_cpuUsage = mgGraphiteAggregator.createMeasure("cpuUsage", "Aggregator - cpuUsage", "num");
  protected final Measure mAggregator_destinations_attemptedRelays = mgGraphiteAggregator.createMeasure("destinations - attemptedRelays", "Aggregator - destinations - attemptedRelays", "num");
  protected final Measure mAggregator_destinations_batchesSent = mgGraphiteAggregator.createMeasure("destinations - batchesSent", "Aggregator - destinations - batchesSent", "num");
  protected final Measure mAggregator_destinations_relayMaxQueueLength = mgGraphiteAggregator.createMeasure("destinations - relayMaxQueueLength", "Aggregator - destinations - relayMaxQueueLength", "num");
  protected final Measure mAggregator_destinations_sent = mgGraphiteAggregator.createMeasure("destinations - sent", "Aggregator - destinations - sent", "num");
  protected final Measure mAggregator_memUsage = mgGraphiteAggregator.createMeasure("memUsage", "Aggregator - memUsage", "num");
  protected final Measure mAggregator_metricsReceived = mgGraphiteAggregator.createMeasure("metricsReceived", "Aggregator - metricsReceived", "num");
  protected final Measure mAggregator_whitelistRejects = mgGraphiteAggregator.createMeasure("whitelistRejects", "Aggregator - whitelistRejects", "num");
  //
  protected final MetricGroup mgGraphiteStats = new MetricGroup(mgGraphite, "Graphite Stats", "Graphite - Stats - {0}");
  protected final Measure mStats_deamon_calculationtime = mgGraphiteStats.createMeasure("deamon.calculationtime", "Stats - deamom - calculationtime", "num");
  protected final Measure mStats_deamon_flush_length = mgGraphiteStats.createMeasure("deamon.flush_length", "Stats - deamom - flush_length", "num");
  protected final Measure mStats_deamon_flush_time = mgGraphiteStats.createMeasure("deamon.flush_time", "Stats - deamom - flush_time", "num");
  protected final Measure mStats_deamon_last_exception = mgGraphiteStats.createMeasure("deamon.last_exception", "Stats - deamom - last_exception", "num");
  protected final Measure mStats_deamon_last_flush = mgGraphiteStats.createMeasure("deamon.last_flush", "Stats - deamom - last_flush", "num");
  protected final Measure mStats_deamon_processing_time = mgGraphiteStats.createMeasure("deamon.processing_time", "Stats - deamom - processing_time", "num");
  protected final Measure mStats_numStats = mgGraphiteStats.createMeasure("numStats", "Stats - numStats", "num");
  protected final Measure mStats_timestamp_lag = mgGraphiteStats.createMeasure("timestamp_lag", "Stats - timestamp_lag", "num");
  protected final Measure mStats_response = mgGraphiteStats.createMeasure("response", "Stats - response", "ms");
  protected final Measure mStats_deamon_bad_lines_seen = mgGraphiteStats.createMeasure("deamon.bad_lines_seen", "Stats - deamom - bad_lines_seen", "num");
  protected final Measure mStats_deamon_metrics_received = mgGraphiteStats.createMeasure("deamon.metrics_received", "Stats - deamom - metrics_received", "num");
  protected final Measure mStats_deamon_packets_received = mgGraphiteStats.createMeasure("deamon.packets_received", "Stats - deamom - packets_received", "num");
  protected final Measure mStats_Counts_response = mgGraphiteStats.createMeasure("counts.response", "Stats_counts - response", "ms");
  protected final Measure mStats_Counts_deamom_bad_lines_seen = mgGraphiteStats.createMeasure("counts.deamon.bad_lines_seen", "Stats_counts - deamom - bad_lines_seen", "num");
  protected final Measure mStats_Counts_deamom_metrics_received = mgGraphiteStats.createMeasure("counts.deamon.metrics_received", "Stats_counts - deamom - metrics_received", "num");
  protected final Measure mStats_Counts_deamom_packets_received = mgGraphiteStats.createMeasure("counts.deamon.packets_received", "Stats_counts - deamom - packets_received", "num");
  protected final Measure mStats_timers_count = mgGraphiteStats.createMeasure("timers.count", "Stats - timers - count", "num");
  protected final Measure mStats_timers_count_90 = mgGraphiteStats.createMeasure("timers.count_90", "Stats - timers - count_90", "num");
  protected final Measure mStats_timers_count_ps = mgGraphiteStats.createMeasure("timers.count_ps", "Stats - timers - count_ps", "num");
  protected final Measure mStats_timers_lower = mgGraphiteStats.createMeasure("timers.lower", "Stats - timers - lower", "ms");
  protected final Measure mStats_timers_lower_90 = mgGraphiteStats.createMeasure("timers.lower_90", "Stats - timers - lower_90", "ms");
  protected final Measure mStats_timers_lower_ps = mgGraphiteStats.createMeasure("timers.lower_ps", "Stats - timers - lower_ps", "ms");
  protected final Measure mStats_timers_mean = mgGraphiteStats.createMeasure("timers.mean", "Stats - timers - mean", "ms");
  protected final Measure mStats_timers_mean_90 = mgGraphiteStats.createMeasure("timers.mean_90", "Stats - timers - mean_90", "ms");
  protected final Measure mStats_timers_mean_ps = mgGraphiteStats.createMeasure("timers.mean_ps", "Stats - timers - mean_ps", "ms");
  protected final Measure mStats_timers_median = mgGraphiteStats.createMeasure("timers.median", "Stats - timers - median", "ms");
  protected final Measure mStats_timers_std = mgGraphiteStats.createMeasure("timers.std", "Stats - timers - std", "ms");
  protected final Measure mStats_timers_sum = mgGraphiteStats.createMeasure("timers.sum", "Stats - timers - sum", "ms");
  protected final Measure mStats_timers_sum_90 = mgGraphiteStats.createMeasure("timers.sum_90", "Stats - timers - sum_90", "ms");
  protected final Measure mStats_timers_sum_ps = mgGraphiteStats.createMeasure("timers.sum_ps", "Stats - timers - sum_ps", "ms");
  protected final Measure mStats_timers_sum_squares = mgGraphiteStats.createMeasure("timers.sum_squares", "Stats - timers - sum_squares", "num");
  protected final Measure mStats_timers_sum_squares_90 = mgGraphiteStats.createMeasure("timers.sum_squares_90", "Stats - timers - sum_squares_90", "num");
  protected final Measure mStats_timers_sum_squares_ps = mgGraphiteStats.createMeasure("timers.sum_squares_ps", "Stats - timers - sum_squares_ps", "num");
  protected final Measure mStats_timers_upper = mgGraphiteStats.createMeasure("timers.upper", "Stats - timers - upper", "ms");
  protected final Measure mStats_timers_upper_90 = mgGraphiteStats.createMeasure("timers.upper_90", "Stats - timers - upper_90", "ms");
  protected final Measure mStats_timers_upper_ps = mgGraphiteStats.createMeasure("timers.upper_ps", "Stats - timers - upper_ps", "ms");
  protected final Measure mStats_Others = mgGraphiteStats.createMeasure("Others", "Others - Stats", "num");
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
  static { // Configuration metadata
    GraphiteMonitor._graphiteURL.setLabel("Graphite URL").setDescription("Enter the URL to Graphite service. {host} will be replaced with target host. Example: 'http://{host}:8888'.");
    GraphiteMonitor._allMetrics.setLabel("All metrics extraction").setDescription("All metrics extraction");
    GraphiteMonitor._systemMetrics.setLabel("System metrics extraction").setDescription("System metrics extraction");
    GraphiteMonitor._customMetrics.setLabel("Custom metrics extraction").setDescription("Custom metrics extraction");
    GraphiteMonitor._target.setLabel("Custom metrics to extract").setDescription("Custom metrics to extract");
    GraphiteMonitor._mapping.setLabel("Custom metrics mapping").setDescription("Custom metrics mapping");
    GraphiteMonitor._from.setLabel("Time range (from)").setDescription("Time range (from)");
    GraphiteMonitor._until.setLabel("Time range (to)").setDescription("Time range (to)");
    GraphiteMonitor._groupLevel.setLabel("Group level").setDescription("Group level");
    GraphiteMonitor._tagPrefix.setLabel("Tag prefix").setDescription("Tag prefix");
  }
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

  private void decodeRules(final String rules, final List<RuleEntry> result, final boolean isSystemRule) throws CommandException {
    final int base = isSystemRule ? 2 : 1;
    final int cnt = isSystemRule ? 4 : 3;
    for (final String x : LibParser.splitWithNL(rules, (char)0)) {
      if (LibStr.isEmptyOrNull(x)) {
        continue;
      }
      final List<String> r = LibParser.split(x.trim(), ';', '"');
      if (r.size() != cnt) {
        continue;
      }
      final RuleEntry e = new RuleEntry();
      e.regex = Pattern.compile(r.get(0));
      if (isSystemRule) {
        e.metric = mgGraphite.find(r.get(1), false, true);
      }
      else {
        e.metric = mServerResult;
      }
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

  private void decodeMetrics(final String metrics, final List<String> result) {
    for (final String x : LibParser.splitWithNL(metrics, (char)0)) {
      if (LibStr.isEmptyOrNull(x)) {
        continue;
      }
      result.add(x.trim());
    }
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
            final String json = response.getOutput();
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
    final List<RuleEntry> result = new ArrayList<>();
    if (config_customMetrics) {
      result.addAll(customRules);
    }
    result.addAll(systemRules);
    return result;
  }

  private List<String> getTargets() {
    if (config_allMetrics) { return getIndex(); }
    final List<String> result = new ArrayList<>();
    if (config_systemMetrics) {
      result.addAll(systemMetrics);
    }
    if (config_customMetrics) {
      result.addAll(customMetrics);
    }
    return result;
  }

  private List<String> getIndex() {
    fetcher.setURL(baseURL + "/metrics/index.json");
    final ReturnObject response = httpCall(fetcher);
    if (response.getRetCode() < 400) { return GraphiteMonitor.toList(new JSONArray(response.getOutput())); }
    return null;
  }

  private void parseJSON(final JSONArray obj, final List<RuleEntry> rules) {
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

  private void processTarget(final String target, final List<RuleEntry> rules, final JSONArray datapoints, final JSONObject tags) {
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

  private void processEntry(final String target, final List<RuleEntry> rules, final double v, final long t, final JSONObject tags) {
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
    if (tags != null) {
      addTags(m, tags);
    }
  }

  private void addTags(final IMetric<?> m, final JSONObject tags) {
    final Tags metricTag = m.getTags();
    for (final String tagName : tags.keySet()) {
      final String tagVal = tags.getString(tagName);
      metricTag.add(LibStr.isEmptyOrNull(config_tagPrefix) ? tagName : config_tagPrefix + tagName, tagVal);
    }
  }

  private RuleEntry findEntry(final String target, final List<RuleEntry> rules) {
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
