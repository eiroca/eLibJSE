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
import com.google.gson.JsonElement;
import net.eiroca.ext.library.elastic.ElasticBulk;
import net.eiroca.ext.library.gson.SimpleJson;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.metrics.datum.IDatum;
import net.eiroca.library.sysadm.monitoring.api.IMeasureConsumer;
import net.eiroca.library.system.IContext;
import net.eiroca.library.system.Logs;

public class GenericConsumer implements IMeasureConsumer, Runnable {

  private static final String CFG_EXPORTER_ELASTIC = "elasticURL";
  private static final String CFG_EXPORTER_ELASTICINDEX = "elasticIndex";
  private static final String CFG_EXPORTER_LOGGER = "logger";

  private static final String ARRAY_SUFFIX = "[]";
  private static final SimpleDateFormat ISO8601_FULL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  private String configElasticServerURL;
  private String configElasticIndex;
  private String configLoggerName;

  protected IContext context = null;
  protected Logger metricLog = null;
  protected ElasticBulk elasticServer = null;

  private final Object dataLock = new Object();
  private List<SimpleJson> buffer = new ArrayList<>();

  public GenericConsumer() {
    super();
  }

  @Override
  public void setup(final IContext context) throws Exception {
    this.context = context;
    configElasticServerURL = context.getConfigString(GenericConsumer.CFG_EXPORTER_ELASTIC, null);
    configElasticIndex = context.getConfigString(GenericConsumer.CFG_EXPORTER_ELASTICINDEX, "metrics-");
    configLoggerName = context.getConfigString(GenericConsumer.CFG_EXPORTER_LOGGER, "Metrics");
    metricLog = LibStr.isNotEmptyOrNull(configLoggerName) ? Logs.getLogger(configLoggerName) : null;
    elasticServer = LibStr.isNotEmptyOrNull(configElasticServerURL) ? new ElasticBulk(configElasticServerURL) : null;
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

  public void addMeasure(final SimpleJson data) {
    synchronized (dataLock) {
      buffer.add(data);
    }
  }

  public List<SimpleJson> swap() {
    List<SimpleJson> result = null;
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
    final List<SimpleJson> events = swap();
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

  private void flush(final List<SimpleJson> events) throws Exception {
    context.debug("flush events");
    final String _type = "log";
    final String _pipeline = null;
    for (final SimpleJson json : events) {
      final String _doc = json.toString();
      try {
        if (metricLog != null) {
          metricLog.info(_doc);
        }
        if (elasticServer != null) {
          final JsonElement dateJson = json.getRoot().get("datetime");
          if (dateJson != null) {
            final String eventDate = dateJson.getAsString().substring(0, 10);
            final String _indexName = configElasticIndex + eventDate;
            final String _id = UUID.randomUUID().toString();
            elasticServer.add(_indexName, _type, _id, _pipeline, _doc);
          }
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

  @Override
  public boolean exportData(final String group, final String metric, final String splitGroup, final String splitName, final IDatum datum, final Map<String, Object> meta) {
    context.debug("exportData ", metric);
    final SimpleJson json = new SimpleJson(true);
    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("CET"));
    cal.setTime(new Date(datum.getTimeStamp()));
    json.addProperty("datetime", cal.getTime(), GenericConsumer.ISO8601_FULL);
    json.addProperty("group", group);
    json.addProperty("metric", metric);
    json.addProperty("value", datum.getValue());
    if (splitGroup != null) {
      json.addProperty("master", false);
      json.addProperty("splitGroup", splitGroup);
      json.addProperty("splitName", splitName);
    }
    else {
      json.addProperty("master", true);
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
          json.addProperty(key, (String[])val);
        }
        else {
          json.addProperty(key, val.toString());
        }
      }
    }
    addMeasure(json);
    return true;
  }

}
