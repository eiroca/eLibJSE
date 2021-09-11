/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.ILog;

public class FLUMEServerMonitor extends RESTServerMonitor {

  /* @formatter:off
    Each Source, Sink or Channel will provide metrics like this:
    {
      "CHANNEL.fc1": {
        "ChannelCapacity": "1000000",
        "ChannelFillPercentage": "0.0",
        "ChannelSize":
        "0",
        "EventPutAttemptCount": "0",
        "EventPutSuccessCount": "0",
        "EventTakeAttemptCount": "3203",
        "EventTakeSuccessCount": "0",
        "StartTime": "1367940231789",
        "StopTime": "0",
        "Type": "CHANNEL"
      }
    }
    @formatter:on */
  static public class JSONMapping {

    IMetric<?> measure;
    double scale;

    public JSONMapping(final Measure measure, final double scale) {
      this.measure = measure;
      this.scale = scale;
    }
  }

  private static final String CONFIG_PORT = "port";

  // measurement variables
  protected final MetricGroup mgFLUME = new MetricGroup(mgMonitor, "FLUME Statistics");
  protected final Measure mAppendAcceptedCount = mgFLUME.createMeasure("Append Accepted", MetricAggregation.zero, "Append Accepted Count", "number");
  protected final Measure mAppendBatchAcceptedCount = mgFLUME.createMeasure("Append Batch Accepted", MetricAggregation.zero, "Append batch accepted count", "number");
  protected final Measure mAppendBatchReceivedCount = mgFLUME.createMeasure("Append Batch Received", MetricAggregation.zero, "Append batch received count", "number");
  protected final Measure mAppendReceivedCount = mgFLUME.createMeasure("Append Received", MetricAggregation.zero, "Append received count", "number");
  protected final Measure mBatchCompleteCount = mgFLUME.createMeasure("Batch Complete", MetricAggregation.zero, "Batch complete count", "number");
  protected final Measure mBatchEmptyCount = mgFLUME.createMeasure("Batch Empty", MetricAggregation.zero, "Batch empty count", "number");
  protected final Measure mBatchUnderflowCount = mgFLUME.createMeasure("Batch Underflow", MetricAggregation.zero, "Batch underflow count", "number");
  protected final Measure mChannelCapacity = mgFLUME.createMeasure("Channel Capacity", MetricAggregation.zero, "Channel capacity", "number");
  protected final Measure mChannelFillPercentage = mgFLUME.createMeasure("Channel Fill", MetricAggregation.zero, "Channel fill (%)", "percent");
  protected final Measure mChannelSize = mgFLUME.createMeasure("Channel Size", MetricAggregation.zero, "Channel size", "number");
  protected final Measure mConnectionClosedCount = mgFLUME.createMeasure("Connection closed", MetricAggregation.zero, "Connection Closed count", "number");
  protected final Measure mConnectionCreatedCount = mgFLUME.createMeasure("Connection created", MetricAggregation.zero, "Connection Created count", "number");
  protected final Measure mConnectionFailedCount = mgFLUME.createMeasure("Connection failed", MetricAggregation.zero, "Connection failed count", "number");
  protected final Measure mEventAcceptedCount = mgFLUME.createMeasure("Event Accepted", MetricAggregation.zero, "Event accepted count", "number");
  protected final Measure mEventDrainAttemptCount = mgFLUME.createMeasure("Event Drain Attempt", MetricAggregation.zero, "Event drain attempt count", "number");
  protected final Measure mEventDrainSuccessCount = mgFLUME.createMeasure("Event Drain Success", MetricAggregation.zero, "Event drain success count", "number");
  protected final Measure mEventPutAttemptCount = mgFLUME.createMeasure("Event Put Attempt", MetricAggregation.zero, "Event put attempt count", "number");
  protected final Measure mEventPutSuccessCount = mgFLUME.createMeasure("Event Put Success", MetricAggregation.zero, "Event put success count", "number");
  protected final Measure mEventReceivedCount = mgFLUME.createMeasure("Event Received", MetricAggregation.zero, "Event received count", "number");
  protected final Measure mEventTakeAttemptCount = mgFLUME.createMeasure("Event Take Attempt", MetricAggregation.zero, "Event Take attempt count", "number");
  protected final Measure mEventTakeSuccessCount = mgFLUME.createMeasure("Event Take Success", MetricAggregation.zero, "Event Take success count", "number");
  protected final Measure mOpenConnectionCount = mgFLUME.createMeasure("Open Connection", MetricAggregation.zero, "Open connection count", "number");
  protected final Measure mStartTime = mgFLUME.createMeasure("Start Time", MetricAggregation.zero, "Start time", "number");
  protected final Measure mStopTime = mgFLUME.createMeasure("Stop Time", MetricAggregation.zero, "Stop time", "number");

  public Map<String, JSONMapping> mappigns = new HashMap<>();

  public FLUMEServerMonitor() {
    super();
    initMapping();
  }

  public void initMapping() {
    mappigns.put("AppendAcceptedCount", new JSONMapping(mAppendAcceptedCount, 1));
    mappigns.put("AppendBatchAcceptedCount", new JSONMapping(mAppendBatchAcceptedCount, 1));
    mappigns.put("AppendBatchReceivedCount", new JSONMapping(mAppendBatchReceivedCount, 1));
    mappigns.put("AppendReceivedCount", new JSONMapping(mAppendReceivedCount, 1));
    mappigns.put("BatchCompleteCount", new JSONMapping(mBatchCompleteCount, 1));
    mappigns.put("BatchEmptyCount", new JSONMapping(mBatchEmptyCount, 1));
    mappigns.put("BatchUnderflowCount", new JSONMapping(mBatchUnderflowCount, 1));
    mappigns.put("ChannelCapacity", new JSONMapping(mChannelCapacity, 1));
    mappigns.put("ChannelFillPercentage", new JSONMapping(mChannelFillPercentage, 1));
    mappigns.put("ChannelSize", new JSONMapping(mChannelSize, 1));
    mappigns.put("ConnectionClosedCount", new JSONMapping(mConnectionClosedCount, 1));
    mappigns.put("ConnectionCreatedCount", new JSONMapping(mConnectionCreatedCount, 1));
    mappigns.put("ConnectionFailedCount", new JSONMapping(mConnectionFailedCount, 1));
    mappigns.put("EventAcceptedCount", new JSONMapping(mEventAcceptedCount, 1));
    mappigns.put("EventDrainAttemptCount", new JSONMapping(mEventDrainAttemptCount, 1));
    mappigns.put("EventDrainSuccessCount", new JSONMapping(mEventDrainSuccessCount, 1));
    mappigns.put("EventPutAttemptCount", new JSONMapping(mEventPutAttemptCount, 1));
    mappigns.put("EventPutSuccessCount", new JSONMapping(mEventPutSuccessCount, 1));
    mappigns.put("EventReceivedCount", new JSONMapping(mEventReceivedCount, 1));
    mappigns.put("EventTakeAttemptCount", new JSONMapping(mEventTakeAttemptCount, 1));
    mappigns.put("EventTakeSuccessCount", new JSONMapping(mEventTakeSuccessCount, 1));
    mappigns.put("OpenConnectionCount", new JSONMapping(mOpenConnectionCount, 1));
    mappigns.put("StartTime", new JSONMapping(mStartTime, 1));
    mappigns.put("StopTime", new JSONMapping(mStopTime, 1));
  }

  @Override
  public URL getURL(final InetAddress host) throws MalformedURLException {
    final String port = "" + context.getConfigInt(FLUMEServerMonitor.CONFIG_PORT, 2000);
    final String urlStr = MessageFormat.format("http://{0}:{1}/metrics", host.getHostName(), port);
    context.debug("FLUME URL: ", urlStr);
    return new URL(urlStr);
  }

  @Override
  public void parseJSON(final JSONObject obj) {
    context.debug("Parsing JSON: ", obj);
    for (final String key : obj.keySet()) {
      final JSONObject node = obj.getJSONObject(key);
      final String type = node.getString("Type");
      final String name = key.startsWith(type + ".") ? key.substring(type.length() + 1) : key;
      for (final String alias : node.keySet()) {
        double val;
        JSONMapping mapping;
        try {
          val = node.getDouble(alias);
          mapping = mappigns.get(alias);
          if (mapping != null) {
            final IMetric<?> m = mapping.measure;
            final IMetric<?> ms = m.getSplitting(type, name);
            ms.setValue(val * mapping.scale);
            context.logF(ILog.LogLevel.debug, "{0} [{1}({2})] -> {3}", m.getMetadata().getDisplayName(), type, name, val * mapping.scale);
          }
        }
        catch (final Exception e) {
        }
      }
    }
  }
}
