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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import net.eiroca.library.data.Status;
import net.eiroca.library.diagnostics.IServerMonitor;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.metrics.datum.Datum;
import net.eiroca.library.metrics.datum.IDatum;
import net.eiroca.library.sysadm.monitoring.api.IMeasureConsumer;
import net.eiroca.library.sysadm.monitoring.api.IMeasureProducer;
import net.eiroca.library.system.IContext;

public class GenericProducer implements IMeasureProducer {

  protected String id;

  protected final String name;
  protected IContext context;
  protected IServerMonitor monitor;
  protected IMeasureConsumer consumer;

  public GenericProducer(final String name, final IServerMonitor monitor, final IMeasureConsumer consumer) {
    super();
    this.name = name;
    this.monitor = monitor;
    this.consumer = consumer;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  @Override
  public void setup(final IContext context) throws Exception {
    this.context = context;
    context.info(name, " setup done");
  }

  @Override
  public void run() {
    try {
      execute();
    }
    catch (final Exception e) {
      context.warn(name + " failed : ", e);
    }
  }

  @Override
  public void teardown() throws Exception {
    context.info(name, " teardown");
  }

  public int exportMeasures(final Map<String, Object> meta, final List<MetricGroup> groups) {
    int result = 0;
    if (groups != null) {
      for (final MetricGroup g : groups) {
        context.trace("processing group: ", g.getName());
        g.refresh();
        for (final IMetric<?> m : g.getMetrics()) {
          result += exportMeasure(meta, g.getName(), m);
        }
      }
    }
    return result;
  }

  public int exportMeasure(final Map<String, Object> meta, final String group, final IMetric<?> m) {
    int result = 0;
    final String metric = m.getMetadata().getInternalName();
    final IDatum value = m.getDatum();
    context.trace("processing metric: ", metric);
    if (value.hasValue()) {
      if (consumer.exportData(group, metric, null, null, value, meta)) {
        result++;
      }
    }
    if (m.hasSplittings()) {
      for (final Entry<String, ?> s : m.getSplittings().entrySet()) {
        final String splitGroup = s.getKey();
        final IMetric<?> split = (IMetric<?>)s.getValue();
        result += exportMesureSplittig(meta, group, metric, splitGroup, split);
      }
    }
    return result;
  }

  private int exportMesureSplittig(final Map<String, Object> meta, final String group, final String metric, final String splitGroup, final IMetric<?> split) {
    int result = 0;
    context.trace("processing metric splitting: ", splitGroup);
    final Datum sum = new Datum();
    for (final Entry<String, ?> sm : split.getSplittings().entrySet()) {
      final String splitName = sm.getKey();
      final IMetric<?> splitValue = (IMetric<?>)sm.getValue();
      final IDatum val = splitValue.getDatum();
      sum.addValue(val.getValue());
      if (consumer.exportData(group, metric, splitGroup, splitName, val, meta)) {
        result++;
      }
    }
    if (!split.getDatum().hasValue()) {
      if (consumer.exportData(group, metric, null, null, sum, meta)) {
        result++;
      }
    }
    return result;
  }

  public Status execute() throws Exception {
    final Map<String, Object> meta = new TreeMap<>();
    final List<String> hosts = new ArrayList<>();
    String aHost = context.getConfigString("host", null);
    if (aHost != null) {
      hosts.add(aHost.trim());
    }
    aHost = context.getConfigString("hosts", null);
    if (aHost != null) {
      for (final String s : aHost.split(" ")) {
        hosts.add(s.trim());
      }
    }
    if (hosts.size() == 0) { return new Status(-1, "Host is missing"); }
    final String tags = context.getConfigString("tags", null);
    meta.put("source", name);
    if (tags != null) {
      meta.put("tags[]", tags.split(" "));
    }
    monitor.setup(context);
    int exported_measure = 0;
    final List<MetricGroup> groups = new ArrayList<>();
    monitor.loadMetricGroup(groups);
    for (final String host : hosts) {
      meta.put("host", host);
      monitor.resetMetrics();
      final boolean ok = monitor.check(host);
      context.info(name, " ", host, " -> ", ok);
      if (ok) {
        exported_measure += exportMeasures(meta, groups);
      }
    }
    return new Status(0, "Exported measures: " + exported_measure);
  }

  @Override
  public String toString() {
    return "GenericMonitor [id=" + id + ", name=" + name + "]";
  }

}
