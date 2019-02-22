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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.system.ILog;

public class FLUMEServerMonitor extends RESTServerMonitor {

  /*
   * Each Source, Sink or Channel will provide metrics like this: { “CHANNEL.fc1”: {
   * “ChannelCapacity”: “1000000”, “ChannelFillPercentage”: “0.0”, “ChannelSize”: “0”,
   * “EventPutAttemptCount”: “0”, “EventPutSuccessCount”: “0”, “EventTakeAttemptCount”: “3203”,
   * “EventTakeSuccessCount”: “0”, “StartTime”: “1367940231789”, “StopTime”: “0”, “Type”: “CHANNEL”
   * } }
   */
  static public class JSONMapping {

    Measure measure;
    double scale;

    public JSONMapping(final Measure measure, final double scale) {
      this.measure = measure;
      this.scale = scale;
    }
  }

  private static final String CONFIG_PORT = "port";

  // measurement variables
  public MeasureGroup mgFLUME = new MeasureGroup("FLUME Monitor", "FLUME - {0}");
  public Measure mAppendAcceptedCount = mgFLUME.createMeasure("Append Accepted");
  public Measure mAppendBatchAcceptedCount = mgFLUME.createMeasure("Append Batch Accepted");
  public Measure mAppendBatchReceivedCount = mgFLUME.createMeasure("Append Batch Received");
  public Measure mAppendReceivedCount = mgFLUME.createMeasure("Append Received");
  public Measure mBatchCompleteCount = mgFLUME.createMeasure("Batch Complete");
  public Measure mBatchEmptyCount = mgFLUME.createMeasure("Batch Empty");
  public Measure mBatchUnderflowCount = mgFLUME.createMeasure("Batch Underflow");
  public Measure mChannelCapacity = mgFLUME.createMeasure("Channel Capacity");
  public Measure mChannelFillPercentage = mgFLUME.createMeasure("Channel Fill");
  public Measure mChannelSize = mgFLUME.createMeasure("Channel Size");
  public Measure mConnectionClosedCount = mgFLUME.createMeasure("Connection Closed");
  public Measure mConnectionCreatedCount = mgFLUME.createMeasure("Connection Created");
  public Measure mConnectionFailedCount = mgFLUME.createMeasure("Connection Failed");
  public Measure mEventAcceptedCount = mgFLUME.createMeasure("Event Accepted");
  public Measure mEventDrainAttemptCount = mgFLUME.createMeasure("Event Drain Attempt");
  public Measure mEventDrainSuccessCount = mgFLUME.createMeasure("Event Drain Success");
  public Measure mEventPutAttemptCount = mgFLUME.createMeasure("Event Put Attempt");
  public Measure mEventPutSuccessCount = mgFLUME.createMeasure("Event Put Success");
  public Measure mEventReceivedCount = mgFLUME.createMeasure("Event Received");
  public Measure mEventTakeAttemptCount = mgFLUME.createMeasure("Event Take Attempt");
  public Measure mEventTakeSuccessCount = mgFLUME.createMeasure("Event Take Success");
  public Measure mOpenConnectionCount = mgFLUME.createMeasure("Open Connection");
  public Measure mStartTime = mgFLUME.createMeasure("Start Time");
  public Measure mStopTime = mgFLUME.createMeasure("Stop Time");

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
  public void loadMetricGroup(final List<MeasureGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgFLUME);
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
            final Measure m = mapping.measure;
            final MeasureSplitting ms = m.getSplitting(type);
            context.logF(ILog.LogLevel.debug, "{0} [{1}({2})] -> {3}", m.getName(), ms.getName(), name, val * mapping.scale);
            ms.setValue(name, val * mapping.scale);
          }
        }
        catch (final Exception e) {
        }
      }
    }
  }
}
