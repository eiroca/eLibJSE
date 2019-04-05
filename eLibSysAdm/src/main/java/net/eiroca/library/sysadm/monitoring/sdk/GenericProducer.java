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
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
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

  private static final String FLD_GROUP = "group";
  private static final String FLD_METRIC = "metric";
  private static final String FLD_MASTER = "master";
  private static final String FLD_SPLIT_GROUP = "splitGroup";
  private static final String FLD_SPLIT_NAME = "splitName";
  private static final String FLD_HOST = "host";
  private static final String FLD_SOURCE = "source";
  private static final String FLD_TAGS = "tags[]";

  protected String id;

  protected final String name;
  protected IContext context;
  protected IServerMonitor monitor;
  protected IMeasureConsumer consumer;
  protected List<String> hosts;
  protected ITagsProvider tagProvider;
  protected List<String> tags = new ArrayList<>();

  public GenericProducer(final String name, final IServerMonitor monitor, final List<String> hosts, final ITagsProvider tagProvider, final IMeasureConsumer consumer) {
    super();
    this.name = name;
    this.monitor = monitor;
    this.hosts = hosts;
    this.tagProvider = tagProvider;
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
    tags.clear();
    final String tagsStr = context.getConfigString("tags", null);
    if (tagsStr != null) {
      for (final String t : tagsStr.split(" ")) {
        tags.add(t);
      }
    }
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

  public static boolean exportData(final IMeasureConsumer consumer, final String group, final String metric, final String splitGroup, final String splitName, final SortedMap<String, Object> meta, final IDatum datum) {
    meta.put(GenericProducer.FLD_GROUP, group);
    meta.put(GenericProducer.FLD_METRIC, metric);
    meta.put(GenericProducer.FLD_MASTER, (splitGroup != null));
    meta.put(GenericProducer.FLD_SPLIT_GROUP, splitGroup);
    meta.put(GenericProducer.FLD_SPLIT_NAME, splitName);
    return consumer.exportDatum(meta, datum);
  }

  public int exportMeasures(final SortedMap<String, Object> meta, final List<MetricGroup> groups) {
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

  public int exportMeasure(final SortedMap<String, Object> meta, final String group, final IMetric<?> m) {
    int result = 0;
    final String metric = m.getMetadata().getInternalName();
    final IDatum value = m.getDatum();
    context.trace("processing metric: ", metric);
    if (value.hasValue()) {
      if (GenericProducer.exportData(consumer, group, metric, null, null, meta, value)) {
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

  private int exportMesureSplittig(final SortedMap<String, Object> meta, final String group, final String metric, final String splitGroup, final IMetric<?> split) {
    int result = 0;
    context.trace("processing metric splitting: ", splitGroup);
    final Datum sum = new Datum();
    for (final Entry<String, ?> sm : split.getSplittings().entrySet()) {
      final String splitName = sm.getKey();
      final IMetric<?> splitValue = (IMetric<?>)sm.getValue();
      final IDatum val = splitValue.getDatum();
      sum.addValue(val.getValue());
      if (GenericProducer.exportData(consumer, group, metric, splitGroup, splitName, meta, val)) {
        result++;
      }
    }
    if (!split.getDatum().hasValue()) {
      if (GenericProducer.exportData(consumer, group, metric, splitGroup, null, meta, sum)) {
        result++;
      }
    }
    return result;
  }

  public Status execute() throws Exception {
    monitor.setup(context);
    int exported_measure = 0;
    final List<MetricGroup> groups = new ArrayList<>();
    monitor.loadMetricGroup(groups);
    for (final String host : hosts) {
      monitor.resetMetrics();
      final boolean ok = monitor.check(host);
      context.info(name, " ", host, " -> ", ok);
      if (ok) {
        final SortedMap<String, Object> meta = new TreeMap<>();
        meta.put(GenericProducer.FLD_SOURCE, name);
        meta.put(GenericProducer.FLD_HOST, host);
        final Set<String> hostTags = (tagProvider != null) ? tagProvider.getTags(host) : null;
        if ((hostTags != null) && (hostTags.size() > 0)) {
          final List<String> _tags = new ArrayList<>();
          _tags.addAll(hostTags);
          if (tags != null) {
            _tags.addAll(tags);
          }
          meta.put(GenericProducer.FLD_TAGS, _tags);
        }
        else {
          if (tags != null) {
            meta.put(GenericProducer.FLD_TAGS, tags);
          }
        }
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
