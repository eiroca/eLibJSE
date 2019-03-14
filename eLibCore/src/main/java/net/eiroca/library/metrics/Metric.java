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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import net.eiroca.library.metrics.datum.IDatum;

public abstract class Metric<M extends IMetric<D>, D extends IDatum> implements IMetric<D> {

  private static final String HEADER_SPLITTINGS = "\"splittings\"";
  private static final String HEADER_VALUE = "\"value\"";
  private static final String TAG_EMPTY = "null";

  transient protected MetricMetadata metadata = null;
  protected UUID id = UUID.randomUUID();
  protected D datum = null;
  protected Map<String, IMetric<D>> splittings = null;

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public MetricMetadata getMetadata() {
    return metadata;
  }

  public abstract M newSplit(String name);

  @Override
  public D getDatum() {
    return datum;
  }

  @Override
  public boolean hasSplittings() {
    return (splittings != null) && (splittings.size() > 0);
  }

  @Override
  public Map<String, IMetric<D>> getSplittings() {
    return splittings;
  }

  @Override
  public IMetric<D> getSplitting(final String... splitNames) {
    IMetric<D> result = this;
    for (String splitName : splitNames) {
      result = result.getSplitting(splitName);
    }
    return result;
  }

  @Override
  public IMetric<D> getSplitting(final String splitName) {
    if (splitName == null) { throw new IllegalArgumentException(); }
    IMetric<D> splitMeasure = null;
    if (splittings == null) {
      splittings = new TreeMap<>();
    }
    else {
      splitMeasure = splittings.get(splitName);
    }
    if (splitMeasure == null) {
      splitMeasure = newSplit(splitName);
      splittings.put(splitName, splitMeasure);
    }
    return splitMeasure;
  }

  @Override
  public void reset() {
    datum.init((metadata != null) ? metadata.getDefValue() : 0.0);
    splittings = null;
  }

  public double getValue(final String split) {
    if (split == null) { throw new IllegalArgumentException(); }
    final IMetric<D> splitMeasure = getSplitting(split);
    return splitMeasure.getValue();
  }

  public void addValue(final String split, final double value) {
    if (split == null) { throw new IllegalArgumentException(); }
    final IMetric<D> splitMeasure = getSplitting(split);
    splitMeasure.addValue(value);
  }

  public void setValue(final String split, final double value) {
    if (split == null) { throw new IllegalArgumentException(); }
    final IMetric<D> splitMeasure = getSplitting(split);
    splitMeasure.setValue(value);
  }



  @Override
  public void toJson(final StringBuilder sb) {
    final boolean hasVal = datum.hasValue();
    final boolean hasSplit = splittings != null;
    if (!hasVal && !hasSplit) {
      sb.append(Metric.TAG_EMPTY);
      return;
    }
    sb.append('{');
    if (hasVal) {
      sb.append(Metric.HEADER_VALUE).append(':');
      datum.toJson(sb, true);
    }
    if (hasSplit) {
      if (hasVal) {
        sb.append(',');
      }
      sb.append(Metric.HEADER_SPLITTINGS).append(":{");
      boolean first = true;
      for (final Entry<String, IMetric<D>> splitting : splittings.entrySet()) {
        if (first) {
          first = false;
        }
        else {
          sb.append(',');
        }
        sb.append('\"');
        sb.append(splitting.getKey());
        sb.append('\"');
        sb.append(":");
        splitting.getValue().toJson(sb);
      }
      sb.append("}");
    }
    sb.append('}');
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(128);
    toJson(sb);
    return sb.toString();
  }

  // IDatum Interface
  public void init(final double defVal) {
    datum.init(defVal);
  }

  public long getTimeStamp() {
    return datum.getTimeStamp();
  }

  public boolean hasValue() {
    return datum.hasValue();
  }

  public double getValue() {
    return datum.getValue();
  }

  public void setValue(final double value) {
    datum.setValue(value);
  }

  public void addValue(final double value) {
    datum.addValue(value);
  }

  public void toJson(StringBuilder sb, boolean simple) {
    datum.toJson(sb, simple);
  }

}
