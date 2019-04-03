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
package net.eiroca.library.sysadm.monitoring.sdk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import org.slf4j.Logger;
import net.eiroca.ext.library.elastic.ElasticBulk;
import net.eiroca.ext.library.gson.SimpleJson;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.metrics.datum.IDatum;
import net.eiroca.library.sysadm.monitoring.api.IMeasureConsumer;
import net.eiroca.library.system.ContextParameters;
import net.eiroca.library.system.IContext;
import net.eiroca.library.system.Logs;

public class GenericConsumer implements IMeasureConsumer, Runnable {

  private static final String ARRAY_SUFFIX = "[]";
  private static final SimpleDateFormat ISO8601_FULL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  private static final String FLD_DATETIME = "datetime";
  private static final String FLD_GROUP = "group";
  private static final String FLD_METRIC = "metric";
  private static final String FLD_MASTER = "master";
  private static final String FLD_SPLIT_GROUP = "splitGroup";
  private static final String FLD_SPLIT_NAME = "splitName";
  private static final String FLD_VALUE = "value";

  public static ContextParameters config = new ContextParameters();
  public static StringParameter _timezone = new StringParameter(GenericConsumer.config, "timezone", null);
  //
  public static StringParameter _elasticURL = new StringParameter(GenericConsumer.config, "elasticURL", "http://localhost:9200/_bulk");
  public static StringParameter _elasticIndex = new StringParameter(GenericConsumer.config, "elasticIndex", "metrics-");
  public static IntegerParameter _elasticIndexMode = new IntegerParameter(GenericConsumer.config, "elasticIndexMode", 1, 0, 2);
  public static StringParameter _indexDateFormat = new StringParameter(GenericConsumer.config, "indexDateFormat", "yyyy.MM.dd");
  public static StringParameter _elasticType = new StringParameter(GenericConsumer.config, "elasticType", GenericConsumer.FLD_METRIC);
  public static StringParameter _elasticPipeline = new StringParameter(GenericConsumer.config, "elasticPipeline", null);
  //
  public static StringParameter _logger = new StringParameter(GenericConsumer.config, "logger", "Metrics");
  // Dynamic mapped to parameters
  protected String config_elasticURL;
  protected String config_elasticIndex;
  protected int config_elasticIndexMode;// 0 fixed, 1 fixed+now 2 fixed+event.date
  protected String config_indexDateFormat;
  protected String config_elasticType;
  protected String config_elasticPipeline;
  protected String config_logger;
  protected String config_timezone;
  //
  private final Object dataLock = new Object();
  private List<Event> buffer = new ArrayList<>();
  //
  protected IContext context = null;
  protected Logger metricLog = null;
  protected ElasticBulk elasticServer = null;
  protected SimpleDateFormat indexDateFormat;

  public GenericConsumer() {
    super();
  }

  @Override
  public void setup(final IContext context) throws Exception {
    this.context = context;
    GenericConsumer.config.convert(context, null, this, "config_");
    metricLog = LibStr.isNotEmptyOrNull(config_logger) ? Logs.getLogger(config_logger) : null;
    indexDateFormat = new SimpleDateFormat(config_indexDateFormat);
    elasticServer = LibStr.isNotEmptyOrNull(config_elasticURL) ? new ElasticBulk(config_elasticURL) : null;
    if (elasticServer != null) {
      elasticServer.open();
    }
    context.info(this.getClass().getName(), " setup done");
  }

  @Override
  public void teardown() throws Exception {
    context.info(this.getClass().getName(), " teardown");
    if (elasticServer != null) {
      elasticServer.close();
      elasticServer = null;
    }
  }

  public void addMeasure(final long timeStamp, final SimpleJson data) {
    if (data == null) { return; }
    final Event e = new Event(timeStamp, data);
    synchronized (dataLock) {
      buffer.add(e);
    }
  }

  public List<Event> swap() {
    List<Event> result = null;
    synchronized (dataLock) {
      if (buffer.size() > 0) {
        result = buffer;
        buffer = new ArrayList<>();
      }
    }
    return result;
  }

  @Override
  public void run() {
    if (context != null) {
      context.debug("run export");
    }
    final List<Event> events = swap();
    if ((events != null) && (events.size() > 0)) {
      try {
        flush(events);
      }
      catch (final Exception e) {
        context.error("Error exporting measures", e);
        context.info(e.getMessage(), " ", Helper.getStackTraceAsString(e));
      }
    }
  }

  private void flush(final List<Event> events) throws Exception {
    context.debug("flush events");
    for (final Event event : events) {
      final SimpleJson json = event.getData();
      final String _doc = json.toString();
      try {
        if (metricLog != null) {
          metricLog.info(_doc);
        }
        if (elasticServer != null) {
          final String _id = getEventID(event);
          final String _indexName = getEventIndex(event);
          elasticServer.add(_indexName, config_elasticType, _id, config_elasticPipeline, _doc);
        }
      }
      catch (final Exception e) {
        context.error("Error exporting ", _doc, "->", e.getMessage(), " ", Helper.getStackTraceAsString(e));
      }
    }
    if (elasticServer != null) {
      elasticServer.flush();
    }
    context.info("Exported measure(s): " + events.size());
  }

  private String getEventIndex(final Event event) {
    final long timestamp = event.getTimestamp();
    String index;
    switch (config_elasticIndexMode) {
      case 1:
        index = config_elasticIndex + indexDateFormat.format(new Date());
        break;
      case 2:
        index = config_elasticIndex + indexDateFormat.format(new Date(timestamp));
        break;
      default:
        index = config_elasticIndex;
        break;
    }
    return index;
  }

  private String getEventID(final Event event) {
    return UUID.randomUUID().toString();
  }

  @Override
  public boolean exportData(final String group, final String metric, final String splitGroup, final String splitName, final IDatum datum, final Map<String, Object> meta) {
    context.debug("exportData ", metric);
    final SimpleJson json = new SimpleJson(true);
    final Calendar cal = Calendar.getInstance();
    if (config_timezone != null) {
      cal.setTimeZone(TimeZone.getTimeZone(config_timezone));
    }
    cal.setTime(new Date(datum.getTimeStamp()));
    final long timeStamp = cal.getTimeInMillis();
    json.addProperty(GenericConsumer.FLD_DATETIME, cal.getTime(), GenericConsumer.ISO8601_FULL);
    json.addProperty(GenericConsumer.FLD_GROUP, group);
    json.addProperty(GenericConsumer.FLD_METRIC, metric);
    if (splitGroup != null) {
      json.addProperty(GenericConsumer.FLD_MASTER, false);
      json.addProperty(GenericConsumer.FLD_SPLIT_GROUP, splitGroup);
      json.addProperty(GenericConsumer.FLD_SPLIT_NAME, splitName);
    }
    else {
      json.addProperty(GenericConsumer.FLD_MASTER, true);
    }
    if (meta != null) {
      for (final Map.Entry<String, Object> metadata : meta.entrySet()) {
        String key = metadata.getKey();
        final Object val = metadata.getValue();
        if (val == null) {
          continue;
        }
        if (key.endsWith(GenericConsumer.ARRAY_SUFFIX)) {
          key = key.substring(0, key.length() - GenericConsumer.ARRAY_SUFFIX.length());
          if (val instanceof String[]) {
            json.addProperty(key, (String[])val);
          }
          else {
            json.addProperty(key, (List<?>)val);
          }
        }
        else {
          json.addProperty(key, val.toString());
        }
      }
    }
    json.addProperty(GenericConsumer.FLD_VALUE, datum.getValue());
    addMeasure(timeStamp, json);
    return true;
  }

}
