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
package net.eiroca.library.metrics.datum;

import net.eiroca.library.core.LibMath;
import net.eiroca.library.metrics.MetricAggregation;

public class StatisticDatum implements IDatum {

  public int count;
  public long firstDate;
  public long lastDate;
  public double first;
  public double last;
  public double min;
  public double max;
  public double sumX;
  public double sumX2;
  public double sumX3;

  public StatisticDatum() {
    init(0.0);
  }

  @Override
  public void addValue(final double value) {
    count++;
    lastDate = System.currentTimeMillis();
    last = value;
    if (count == 1) {
      min = value;
      max = value;
      first = value;
      firstDate = lastDate;
    }
    else {
      if (value < min) {
        min = value;
      }
      else if (value > max) {
        max = value;
      }
    }
    double t = value;
    sumX += t;
    t *= value;
    sumX2 += t;
    t *= value;
    sumX3 += t;
  }

  @Override
  public void init(final double defVal) {
    count = 0;
    min = 0.0;
    max = 0.0;
    sumX = 0.0;
    sumX2 = 0.0;
    sumX3 = 0.0;
    firstDate = 0;
    lastDate = 0;
  }

  @Override
  public boolean hasValue() {
    return count > 0;
  }

  @Override
  public double getValue() {
    return getAverage();
  }

  public double getValue(final MetricAggregation aggregation) {
    double val = 0;
    switch (aggregation) {
      case min:
        val = min;
        break;
      case max:
        val = max;
        break;
      case first:
        val = first;
        break;
      case last:
        val = last;
        break;
      case sum:
        val = sumX;
        break;
      case count:
        val = count;
        break;
      case average:
        val = getAverage();
        break;
      case stddev:
        val = getStdDev();
        break;
    }
    return val;
  }

  @Override
  public void setValue(final double value) {
    addValue(value);
  }

  @Override
  public String toString() {
    return String.format("CNT=%d, AVG=%.2f, MIN=%.2f, MAX=%.2f", count, count > 0 ? (sumX / count) : "N.A.", min, max);
  }

  public int getCount() {
    return count;
  }

  public double getFirst() {
    return first;
  }

  public double getLast() {
    return last;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }

  public double getAverage() {
    return (count > 0) ? sumX / count : 0;
  }

  public double getStdDev() {
    return LibMath.stddev(count, sumX, sumX2);
  }

  @Override
  public void toJson(final StringBuilder sb, final boolean simple) {
    sb.append("{");
    sb.append("\"count\":").append(count).append(',');
    if (!simple) {
      sb.append("\"firstDate\":").append(firstDate).append(',');
      sb.append("\"lastDate\":").append(lastDate).append(',');
    }
    sb.append("\"first\":").append(first).append(',');
    sb.append("\"last\":").append(last).append(',');
    sb.append("\"min\":").append(min).append(',');
    sb.append("\"max\":").append(max).append(',');
    sb.append("\"sumX\":").append(sumX).append(',');
    sb.append("\"sumX2\":").append(sumX2).append(',');
    sb.append("\"sumX3\":").append(sumX3);
    sb.append("}");
  }

}
