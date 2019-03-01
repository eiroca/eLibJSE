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

public class MeasureGroup {

  private final List<Measure> metrics = new ArrayList<>();
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

  public List<Measure> getMetrics() {
    return metrics;
  }

  public MeasureGroup(final String name) {
    this(name, "{0}");
  }

  public MeasureGroup(final String name, final String measureNameFormat) {
    this.name = name;
    this.measureNameFormat = measureNameFormat;
  }

  public Measure add(final Measure metric) {
    metrics.add(metric);
    return metric;
  }

  public Measure remove(final Measure metric) {
    metrics.remove(metric);
    return metric;
  }

  private Measure find(final String measureName, final boolean createIfMissing) {
    Measure m = null;
    for (final Measure cur : metrics) {
      if (cur.getName().equals(measureName)) {
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
    final Measure m = find(metricName, true);
    m.setValue(metricValue);
  }

  public void setValue(final String metricName, final String splitName, final String split, final double metricValue) {
    final Measure m = find(metricName, true);
    final MeasureSplitting ms = m.getSplitting(splitName);
    ms.setValue(split, metricValue);
  }

  public void reset() {
    for (final Measure m : metrics) {
      m.reset();
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('"').append(name).append("\":{");
    boolean first = true;
    for (final Measure m : metrics) {
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
    for (final Measure m : metrics) {
      if (m instanceof IDerivedMeasure) {
        ((IDerivedMeasure)m).refresh();
      }
    }
  }

  public Measure createMeasure(final String name) {
    final MeasureMetadata definition = new MeasureMetadata(name, measureNameFormat, 0);
    return new Measure(this, definition);
  }

  public Measure createMeasure(final String name, final double defValue) {
    final MeasureMetadata definition = new MeasureMetadata(name, measureNameFormat, defValue);
    return new Measure(this, definition);

  }

}