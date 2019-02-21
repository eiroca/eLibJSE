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
package net.eiroca.library.measure;

import java.util.Collection;
import java.util.HashMap;

public class GenericAggregatedMeasure {

  transient public String measureName;

  public int count;
  public double first;
  public double last;
  public double min;
  public double max;
  public double sumX;
  public double sumX2;
  public double sumX3;

  public HashMap<String, GenericAggregatedMeasure> splittings = null;

  public GenericAggregatedMeasure(final String name) {
    measureName = name;
    count = 0;
    min = 0.0;
    max = 0.0;
    sumX = 0.0;
    sumX2 = 0.0;
    sumX3 = 0.0;
  }

  public void addValue(final double value, final String splitName) {
    if (splitName != null) {
      final GenericAggregatedMeasure splitMeasure = getSplitting(splitName);
      if (splitMeasure != null) {
        splitMeasure.addValue(value);
      }
    }
    addValue(value);
  }

  public void addValue(final double value) {
    double t = value;
    count++;
    if (count == 1) {
      min = value;
      max = value;
      first = value;
    }
    else {
      if (value < min) {
        min = value;
      }
      else if (value > max) {
        max = value;
      }
    }
    last = value;
    sumX += t;
    t *= t;
    sumX2 += t;
    t *= t;
    sumX3 += t;
  }

  public void cleanValue() {
    count = 0;
    if (splittings != null) {
      for (final GenericAggregatedMeasure splitMeasure : splittings.values()) {
        splitMeasure.cleanValue();
      }
    }
  }

  public boolean hasValue() {
    return (count > 0);
  }

  public GenericAggregatedMeasure getSplitting(final String splitName) {
    GenericAggregatedMeasure splitMeasure = null;
    if (splitName != null) {
      if (splittings == null) {
        splittings = new HashMap<>();
      }
      else {
        splitMeasure = splittings.get(splitName);
      }

      if (splitMeasure == null) {
        splitMeasure = new GenericAggregatedMeasure(splitName);
        splittings.put(splitName, splitMeasure);
      }
    }
    return splitMeasure;
  }

  public Collection<GenericAggregatedMeasure> getSplitMeasures() {
    return splittings == null ? null : splittings.values();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(64);
    sb.append(String.format("%s: CNT=%d, AVG=%.2f, MIN=%.2f, MAX=%.2f", measureName, count, count > 0 ? (sumX / count) : "N.A.", min, max));
    if (splittings != null) {
      for (final GenericAggregatedMeasure splitMeasure : splittings.values()) {
        sb.append("\n -- ");
        sb.append(splitMeasure.toString());
      }
    }
    return sb.toString();
  }

  public double getAverage() {
    return (count > 0) ? sumX / count : 0;
  }

  public double getStdDev() {
    return (count > 0) ? Math.sqrt((count * sumX2) - (sumX * sumX)) / count : 0;
  }

}
