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

  protected final UUID id = UUID.randomUUID();
  protected MetricMetadata metadata = null;
  protected D datum = null;
  protected Map<String, M> splittings = null;

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

  public boolean hasSplittings() {
    return (splittings != null) && (splittings.size() > 0);
  }

  public Map<String, M> getSplittings() {
    return splittings;
  }

  @Override
  public M getSplitting(final String splitName) {
    if (splitName == null) { throw new IllegalArgumentException(); }
    M splitMeasure = null;
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
    final M splitMeasure = getSplitting(split);
    return splitMeasure.getDatum().getValue();
  }

  public void addValue(final String split, final double value) {
    if (split == null) { throw new IllegalArgumentException(); }
    final M splitMeasure = getSplitting(split);
    splitMeasure.getDatum().addValue(value);
  }

  public void setValue(final String split, final double value) {
    if (split == null) { throw new IllegalArgumentException(); }
    final M splitMeasure = getSplitting(split);
    splitMeasure.getDatum().setValue(value);
  }

  public void setValue(final double value) {
    datum.setValue(value);
  }

  public void addValue(final double value) {
    datum.addValue(value);
  }

  public boolean hasValue() {
    return datum.hasValue();
  }

  public double getValue() {
    return datum.getValue();
  }

  public String getName() {
    String name = null;
    if (metadata != null) {
      name = metadata.getDisplayName();
    }
    return name;
  }

  @Override
  public void toJson(final StringBuilder sb) {
    final String name = getName();
    final boolean hasVal = datum.hasValue();
    final boolean hasName = name != null;
    final boolean hasSplit = splittings != null;
    if (hasName || hasSplit) {
      sb.append("{");
    }
    if (hasVal) {
      if (hasName) {
        sb.append('\"');
        sb.append(name);
        sb.append('\"');
        sb.append(":");
      }
      else if (hasSplit) {
        sb.append("\"value:\"");
      }
      datum.toJson(sb, true);
    }
    if (hasSplit) {
      if (hasVal) {
        sb.append(',');
      }
      boolean first = true;
      for (final Entry<String, M> splitting : splittings.entrySet()) {
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
      if (hasVal) {
      }
    }
    if (hasName || hasSplit) {
      sb.append('}');
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(128);
    toJson(sb);
    return sb.toString();
  }

}
