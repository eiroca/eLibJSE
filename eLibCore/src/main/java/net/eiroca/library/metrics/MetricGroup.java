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
package net.eiroca.library.metrics;

import java.util.ArrayList;
import java.util.List;
import net.eiroca.library.metrics.derived.IDerivedMeasure;

public class MetricGroup {

  private final List<IMetric<?>> metrics = new ArrayList<>();
  private String name;
  private String measureNameFormat = "{0}";

  public String getMeasureNameFormat() {
    return measureNameFormat;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<IMetric<?>> getMetrics() {
    return metrics;
  }

  public MetricGroup(final String name) {
    this(name, "{0}");
  }

  public MetricGroup(final String name, final String measureNameFormat) {
    this.name = name;
    this.measureNameFormat = measureNameFormat;
  }

  public Measure add(final Measure measure) {
    metrics.add(measure);
    return measure;
  }

  public Measure remove(final Measure measure) {
    metrics.remove(measure);
    return measure;
  }

  public boolean isNamed(final IMetric<?> m, final String name) {
    if (m.getMetadata() != null) {
      final String x = m.getMetadata().getInternalName();
      return x.equals(name);
    }
    return false;
  }

  public IMetric<?> find(final String measureName, final boolean createIfMissing) {
    IMetric<?> m = null;
    for (final IMetric<?> cur : metrics) {
      if (isNamed(cur, measureName)) {
        m = cur;
        break;
      }
    }
    if ((m == null) && createIfMissing) {
      m = createMeasure(measureName, 0);
    }
    return m;
  }

  public void setValue(final String metricName, final double metricValue) {
    final IMetric<?> m = find(metricName, true);
    m.setValue(metricValue);
  }

  public void setValue(final String metricName, final String splitName, final String split, final double metricValue) {
    final IMetric<?> m = find(metricName, true);
    final IMetric<?> ms = m.getSplitting(splitName, split);
    ms.setValue(metricValue);
  }

  public void reset() {
    for (final IMetric<?> m : metrics) {
      m.reset();
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('"').append(name).append("\":{");
    boolean first = true;
    for (final IMetric<?> m : metrics) {
      if (!first) {
        sb.append(',');
      }
      else {
        first = false;
      }
      sb.append(m.toString());
    }
    sb.append('}');
    return sb.toString();
  }

  public void refresh() {
    for (final IMetric<?> m : metrics) {
      if (m instanceof IDerivedMeasure) {
        ((IDerivedMeasure)m).refresh();
      }
    }
  }

  public Measure define(final String name, final Measure m) {
    final MetricMetadata definition = new MetricMetadata(name, measureNameFormat, 0);
    m.metadata = definition;
    return add(m);
  }

  public Measure define(final String name, final Measure m, final String description, final String unit, final String rate) {
    final MetricMetadata definition = new MetricMetadata(name, measureNameFormat, 0);
    definition.setDescription(description);
    definition.setUnit(unit);
    definition.setRate(rate);
    m.metadata = definition;
    return null;
  }

  public Measure createMeasure(final String name) {
    final MetricMetadata definition = new MetricMetadata(name, measureNameFormat, 0);
    return add(new Measure(definition));
  }

  public Measure createMeasure(final String name, final double defValue) {
    final MetricMetadata definition = new MetricMetadata(name, measureNameFormat, defValue);
    return add(new Measure(definition));
  }

  public Measure createMeasure(final String name, final String description, final String unit) {
    final MetricMetadata definition = new MetricMetadata(name, measureNameFormat, 0);
    definition.setDescription(description);
    definition.setUnit(unit);
    return add(new Measure(definition));
  }

  public Measure createMeasure(final String name, final String description, final String unit, final String rate) {
    final MetricMetadata definition = new MetricMetadata(name, measureNameFormat, 0);
    definition.setDescription(description);
    definition.setUnit(unit);
    definition.setRate(rate);
    return add(new Measure(definition));
  }

  public Measure createMeasure(final String name, final String description, final String unit, final boolean calcDelta) {
    final MetricMetadata definition = new MetricMetadata(name, measureNameFormat, 0);
    definition.setDescription(description);
    definition.setUnit(unit);
    definition.setCalcDelta(calcDelta);
    return add(new Measure(definition));
  }

}
