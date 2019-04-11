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
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import net.eiroca.ext.library.gson.SimpleJson;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.metrics.datum.IDatum;
import net.eiroca.library.sysadm.monitoring.api.DatumCheck;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.sysadm.monitoring.api.EventRule;
import net.eiroca.library.sysadm.monitoring.api.IExporter;
import net.eiroca.library.sysadm.monitoring.api.IMeasureConsumer;
import net.eiroca.library.sysadm.monitoring.sdk.exporter.Exporters;
import net.eiroca.library.system.ContextParameters;
import net.eiroca.library.system.IContext;

public class GenericConsumer implements IMeasureConsumer, Runnable {

  private static final String CONFIG_PREFIX = null;
  private static final String ARRAY_SUFFIX = "[]";
  private static final SimpleDateFormat ISO8601_FULL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  private static final String FLD_DATETIME = "@timestamp";
  private static final String FLD_VALUE = "value";
  private static final String FLD_STATUS = "status";
  private static final String STATUS_OK = "OK";
  private static final String FLD_STATUS_DESC = "violation";
  private static final String FLD_STATUS_MINVAL = "minval";
  private static final String FLD_STATUS_MAXVAL = "maxval";

  public static ContextParameters config = new ContextParameters();
  public static StringParameter _timezone = new StringParameter(GenericConsumer.config, "timezone", null);
  // Dynamic mapped to parameters
  protected String config_timezone;
  //
  private final Object dataLock = new Object();
  private List<Event> buffer = new ArrayList<>();
  //
  protected IContext context = null;
  protected RuleEngine ruleEngine;
  protected Map<String, String> alias;

  private static List<IExporter> exporters = new ArrayList<>();
  static {
    for (String name : Exporters.registry.getNames()) {
      GenericConsumer.exporters.add(Exporters.newInstance(name));
    }
  }

  public GenericConsumer(final RuleEngine ruleEngine, final Map<String, String> alias) {
    this.ruleEngine = ruleEngine;
    this.alias = alias;
  }

  @Override
  public void setup(final IContext context) throws Exception {
    this.context = context;
    GenericConsumer.config.convert(context, GenericConsumer.CONFIG_PREFIX, this, "config_");
    for (final IExporter connector : GenericConsumer.exporters) {
      connector.setup(context);
    }
    context.info(this.getClass().getName(), " setup done");
  }

  @Override
  public void teardown() throws Exception {
    context.info(this.getClass().getName(), " teardown");
    for (final IExporter connector : GenericConsumer.exporters) {
      connector.teardown();
    }
  }

  public void addMeasure(final EventRule rule, final long timeStamp, final SimpleJson data) {
    if (data == null) { return; }
    final Event e = new Event(timeStamp, data, rule);
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
    final List<IExporter> validConnectors = new ArrayList<>();
    for (final IExporter connector : GenericConsumer.exporters) {
      if (connector.beginBulk()) {
        validConnectors.add(connector);
      }
    }
    for (final Event event : events) {
      final EventRule rule = event.getRule();
      for (final IExporter connector : validConnectors) {
        if (rule.export(connector.getId())) {
          connector.process(event);
        }
      }
    }
    for (final IExporter connector : validConnectors) {
      connector.endBulk();
    }
    context.info("Exported measure(s): " + events.size());
  }

  @Override
  public boolean exportDatum(final SortedMap<String, Object> metadata, final IDatum datum) {
    context.debug("exportData ", datum);
    final EventRule rule = (ruleEngine != null) ? ruleEngine.ruleFor(metadata) : null;
    if (rule == null) { return false; }
    final SimpleJson json = new SimpleJson(true);
    final Calendar cal = Calendar.getInstance();
    if (config_timezone != null) {
      cal.setTimeZone(TimeZone.getTimeZone(config_timezone));
    }
    long timeStamp = datum.getTimeStamp();
    if (timeStamp == 0) {
      timeStamp = System.currentTimeMillis();
    }
    cal.setTime(new Date(timeStamp));
    timeStamp = cal.getTimeInMillis();
    json.addProperty(GenericConsumer.FLD_DATETIME, cal.getTime(), GenericConsumer.ISO8601_FULL);
    if (metadata != null) {
      for (final Map.Entry<String, Object> metaEntry : metadata.entrySet()) {
        String key = metaEntry.getKey();
        Object val = metaEntry.getValue();
        if (val == null) {
          continue;
        }
        if (alias != null) {
          final String aliasKey = key.toLowerCase() + "." + String.valueOf(val).toLowerCase();
          final String newVal = alias.get(aliasKey);
          if (newVal != null) {
            if (LibStr.isEmptyOrNull(newVal)) {
              continue;
            }
            val = newVal;
          }
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
    final Set<DatumCheck> violations = rule.violations(datum);
    if (violations != null) {
      if (violations.size() > 0) {
        double maxW = -1;
        DatumCheck fail = null;
        for (final DatumCheck chk : violations) {
          if (fail == null) {
            fail = chk;
            maxW = fail.getWeight();
          }
          else {
            final double w = chk.getWeight();
            if (w > maxW) {
              maxW = w;
              fail = chk;
            }
          }
        }
        json.set(GenericConsumer.FLD_STATUS, fail.getCheckName().toUpperCase());
        final Double min = fail.getMin();
        if (min != null) {
          json.addProperty(GenericConsumer.FLD_STATUS_MINVAL, min);
        }
        final Double max = fail.getMax();
        if (max != null) {
          json.addProperty(GenericConsumer.FLD_STATUS_MAXVAL, max);
        }
        switch (fail.check(datum)) {
          case MIN:
            json.set(GenericConsumer.FLD_STATUS_DESC, "Value lower than minimum");
            break;
          case MAX:
            json.set(GenericConsumer.FLD_STATUS_DESC, "Value greather than maximum");
            break;
          case BOTH:
            json.set(GenericConsumer.FLD_STATUS_DESC, "Value is invalid");
            break;
          default:
            break;
        }
      }
      else {
        json.set(GenericConsumer.FLD_STATUS, GenericConsumer.STATUS_OK);
      }
    }
    json.addProperty(GenericConsumer.FLD_VALUE, datum.getValue());
    addMeasure(rule, timeStamp, json);
    return true;
  }

}
