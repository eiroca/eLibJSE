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

public class Measure extends SimpleMeasure {

  MeasureGroup group;
  List<MeasureSplitting> splittings = new ArrayList<>();

  public Measure(final MeasureGroup owner, final String name) {
    this(owner, name, 0.0);
  }

  public Measure(final MeasureGroup owner, final String name, final double defValue) {
    super(name, defValue);
    setGroup(owner);
  }

  @Override
  public void reset() {
    super.reset();
    if (splittings != null) {
      splittings.clear();
    }
  }

  public void setGroup(final MeasureGroup aGroup) {
    if (group != null) {
      group.remove(this);
    }
    if (aGroup != null) {
      aGroup.add(this);
    }
    group = aGroup;
  }

  public MeasureGroup getGroup() {
    return group;
  }

  public boolean hasSplittings() {
    return splittings.size() > 0;
  }

  public synchronized MeasureSplitting getSplitting(final String splitName) {
    int idx = -1;
    MeasureSplitting split = null;
    for (int i = 0; i < splittings.size(); i++) {
      split = splittings.get(i);
      if (split.getName().equals(splitName)) {
        idx = i;
        break;
      }
    }
    if (idx < 0) {
      split = new MeasureSplitting(splitName);
      splittings.add(split);
    }
    return split;
  }

  public List<MeasureSplitting> getSplittings() {
    return splittings;
  }

  @Override
  public String getName() {
    return (group != null) ? group.getMeasureName(name) : super.getName();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('"').append(name).append("\":").append(datum.value);
    if (splittings.size() > 0) {
      sb.append(",");
      boolean first = true;
      for (final MeasureSplitting ms : splittings) {
        if (!first) {
          sb.append(',');
        }
        sb.append(ms.toString());
        first = false;
      }
    }
    return sb.toString();
  }
}
