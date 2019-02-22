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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class MeasureSplitting {

  HashMap<String, Datum> metrics = new HashMap<>();
  String spitName;

  public MeasureSplitting(final String splitName) {
    spitName = splitName;
  }

  public void setValue(final String split, final double value) {
    Datum m;
    synchronized (metrics) {
      m = metrics.get(split);
      if (m == null) {
        m = new Datum();
        metrics.put(split, m);
      }
    }
    m.setValue(value);
  }

  public void addValue(final String split, final double value) {
    Datum m;
    synchronized (metrics) {
      m = metrics.get(split);
      if (m == null) {
        m = new Datum();
        metrics.put(split, m);
      }
    }
    m.addValue(value);
  }

  public double getValue(final String split, final double defVal) {
    Datum m;
    double res = defVal;
    synchronized (metrics) {
      m = metrics.get(split);
      if (m != null) {
        res = m.getValue();
      }
    }
    return res;
  }

  public String getName() {
    return spitName;
  }

  public List<Datum> _getSplits() {
    final List<Datum> result = new ArrayList<>();
    for (final Entry<String, Datum> split : metrics.entrySet()) {
      result.add(split.getValue());
    }
    return result;
  }

  public List<String> getSplitNames() {
    final List<String> result = new ArrayList<>();
    for (final Entry<String, Datum> split : metrics.entrySet()) {
      result.add(split.getKey());
    }
    return result;
  }

  public List<SplittedDatum> getSplitings() {
    final List<SplittedDatum> result = new ArrayList<>();
    for (final Entry<String, Datum> split : metrics.entrySet()) {
      result.add(new SplittedDatum(split));
    }
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("\"").append(spitName).append("\":{");
    boolean first = true;
    for (final String m : metrics.keySet()) {
      if (!first) {
        sb.append(',');
      }
      sb.append(metrics.get(m));
      first = false;
    }
    sb.append("}");
    return sb.toString();
  }
}
