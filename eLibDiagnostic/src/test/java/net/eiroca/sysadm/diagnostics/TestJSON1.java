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
package net.eiroca.sysadm.diagnostics;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;

public class TestJSON1 {

  static public class JSONMapping {

    Measure measure;
    double scale;

    public JSONMapping(final Measure measure, final double scale) {
      this.measure = measure;
      this.scale = scale;
    }
  }

  public MetricGroup mgeSysAdm = new MetricGroup("eSysAdm Monitor", "FLUME - {0}");
  public Measure mAppendAcceptedCount = mgeSysAdm.createMeasure("AppendAcceptedCount");
  public Measure mAppendBatchAcceptedCount = mgeSysAdm.createMeasure("AppendBatchAcceptedCount");
  public Measure mAppendBatchReceivedCount = mgeSysAdm.createMeasure("AppendBatchReceivedCount");
  public Measure mAppendReceivedCount = mgeSysAdm.createMeasure("AppendReceivedCount");
  public Measure mBatchCompleteCount = mgeSysAdm.createMeasure("BatchCompleteCount");
  public Measure mBatchEmptyCount = mgeSysAdm.createMeasure("BatchEmptyCount");
  public Measure mBatchUnderflowCount = mgeSysAdm.createMeasure("BatchUnderflowCount");
  public Measure mChannelCapacity = mgeSysAdm.createMeasure("ChannelCapacity");
  public Measure mChannelFillPercentage = mgeSysAdm.createMeasure("ChannelFillPercentage");
  public Measure mChannelSize = mgeSysAdm.createMeasure("ChannelSize");
  public Measure mConnectionClosedCount = mgeSysAdm.createMeasure("ConnectionClosedCount");
  public Measure mConnectionCreatedCount = mgeSysAdm.createMeasure("ConnectionCreatedCount");
  public Measure mConnectionFailedCount = mgeSysAdm.createMeasure("ConnectionFailedCount");
  public Measure mEventAcceptedCount = mgeSysAdm.createMeasure("EventAcceptedCount");
  public Measure mEventDrainAttemptCount = mgeSysAdm.createMeasure("EventDrainAttemptCount");
  public Measure mEventDrainSuccessCount = mgeSysAdm.createMeasure("EventDrainSuccessCount");
  public Measure mEventPutAttemptCount = mgeSysAdm.createMeasure("EventPutAttemptCount");
  public Measure mEventPutSuccessCount = mgeSysAdm.createMeasure("EventPutSuccessCount");
  public Measure mEventReceivedCount = mgeSysAdm.createMeasure("EventReceivedCount");
  public Measure mEventTakeAttemptCount = mgeSysAdm.createMeasure("EventTakeAttemptCount");
  public Measure mEventTakeSuccessCount = mgeSysAdm.createMeasure("EventTakeSuccessCount");
  public Measure mOpenConnectionCount = mgeSysAdm.createMeasure("OpenConnectionCount");
  public Measure mStartTime = mgeSysAdm.createMeasure("StartTime");
  public Measure mStopTime = mgeSysAdm.createMeasure("StopTime");

  public Map<String, JSONMapping> mappigns = new HashMap<>();

  public void initMapping() {
    mappigns.put("AppendAcceptedCount", new JSONMapping(mAppendAcceptedCount, 1));
    mappigns.put("AppendBatchAcceptedCount", new JSONMapping(mAppendBatchAcceptedCount, 1));
    mappigns.put("AppendBatchReceivedCount", new JSONMapping(mAppendBatchReceivedCount, 1));
    mappigns.put("AppendReceivedCount", new JSONMapping(mAppendReceivedCount, 1));
    mappigns.put("BatchCompleteCount", new JSONMapping(mBatchCompleteCount, 1));
    mappigns.put("BatchEmptyCount", new JSONMapping(mBatchEmptyCount, 1));
    mappigns.put("BatchUnderflowCount", new JSONMapping(mBatchUnderflowCount, 1));
    mappigns.put("ChannelCapacity", new JSONMapping(mChannelCapacity, 1));
    mappigns.put("ChannelFillPercentage", new JSONMapping(mChannelFillPercentage, 100));
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

  static String JSON = "{\"CHANNEL.cTrace\":{\"ChannelFillPercentage\":\"1.0\",\"ChannelCapacity\":\"5000000\",\"Type\":\"CHANNEL\",\"ChannelSize\":\"0\",\"EventTakeSuccessCount\":\"48386\",\"StartTime\":\"1487072657957\",\"EventTakeAttemptCount\":\"50681\",\"EventPutSuccessCount\":\"48386\",\"EventPutAttemptCount\":\"48386\",\"StopTime\":\"0\"},\"SINK.kTrace\":{\"ConnectionCreatedCount\":\"0\",\"Type\":\"SINK\",\"ConnectionClosedCount\":\"0\",\"BatchCompleteCount\":\"461\",\"BatchEmptyCount\":\"2244\",\"EventDrainAttemptCount\":\"48386\",\"StartTime\":\"1487072657972\",\"EventDrainSuccessCount\":\"48386\",\"BatchUnderflowCount\":\"50\",\"StopTime\":\"0\",\"ConnectionFailedCount\":\"0\"},\"SOURCE.sTCP\":{\"EventReceivedCount\":\"48386\",\"Type\":\"SOURCE\",\"AppendBatchAcceptedCount\":\"0\",\"EventAcceptedCount\":\"48386\",\"AppendReceivedCount\":\"0\",\"StartTime\":\"1487072658519\",\"OpenConnectionCount\":\"0\",\"AppendAcceptedCount\":\"0\",\"AppendBatchReceivedCount\":\"0\",\"StopTime\":\"0\"},\"SINK.koldLogger\":{\"ConnectionCreatedCount\":\"3\",\"Type\":\"SINK\",\"ConnectionClosedCount\":\"0\",\"BatchCompleteCount\":\"2392\",\"BatchEmptyCount\":\"2246\",\"EventDrainAttemptCount\":\"48386\",\"StartTime\":\"1487072657972\",\"EventDrainSuccessCount\":\"48386\",\"BatchUnderflowCount\":\"48\",\"StopTime\":\"0\",\"ConnectionFailedCount\":\"0\"},\"CHANNEL.cOldLogger\":{\"ChannelFillPercentage\":\"0.0\",\"ChannelCapacity\":\"1000000\",\"Type\":\"CHANNEL\",\"ChannelSize\":\"0\",\"EventTakeSuccessCount\":\"48386\",\"StartTime\":\"1487072657957\",\"EventTakeAttemptCount\":\"50681\",\"EventPutSuccessCount\":\"48386\",\"EventPutAttemptCount\":\"48386\",\"StopTime\":\"0\"}}";

  public static void main(final String[] args) {
    final TestJSON1 me = new TestJSON1();
    me.initMapping();

    final JSONObject obj = new JSONObject(TestJSON1.JSON);
    for (final String key : obj.keySet()) {
      final JSONObject node = obj.getJSONObject(key);
      final String type = node.getString("Type");
      final String name = key.startsWith(type + ".") ? key.substring(type.length() + 1) : key;
      for (final String alias : node.keySet()) {
        double val;
        JSONMapping mapping;
        try {
          val = node.getDouble(alias);
          mapping = me.mappigns.get(alias);
          if (mapping != null) {
            final Measure m = mapping.measure.getSplitting(type);
            m.setValue(name, val * mapping.scale);
          }
        }
        catch (final Exception e) {
        }
      }
    }
    System.out.println(me.mgeSysAdm);
  }

}
