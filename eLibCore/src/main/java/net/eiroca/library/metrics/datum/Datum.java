/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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

public class Datum implements IDatum {

  public long timestamp;
  public double value;

  public Datum() {
    value = 0;
    timestamp = 0;
  }

  public Datum(final double value) {
    setValue(value);
  }

  public Datum(final IDatum m) {
    if (m.hasValue()) {
      timestamp = m.getTimeStamp();
      value = m.getValue();
    }
    else {
      timestamp = System.currentTimeMillis();
      value = 0;
    }
  }

  @Override
  public void init(final double defVal) {
    timestamp = 0;
    value = defVal;
  }

  @Override
  public String toString() {
    return (timestamp == 0) ? "?" : String.valueOf(value);
  }

  @Override
  public long getTimeStamp() {
    return timestamp;
  }

  @Override
  public boolean hasValue() {
    return timestamp != 0;
  }

  @Override
  public double getValue() {
    return value;
  }

  @Override
  public void setValue(final double value) {
    this.value = value;
    timestamp = System.currentTimeMillis();
  }

  @Override
  public void setValue(final long timestamp, final double value) {
    this.timestamp = timestamp;
    this.value = value;
  }

  public void addValue(final Datum datum) {
    if (timestamp == 0) {
      timestamp = datum.timestamp != 0 ? datum.timestamp : System.currentTimeMillis();
    }
    value += datum.value;
  }

  @Override
  public void addValue(final double value) {
    if (timestamp == 0) {
      timestamp = System.currentTimeMillis();
    }
    this.value += value;
  }

  @Override
  public void toJson(final StringBuilder sb, final boolean simple) {
    if (!simple) {
      sb.append('{');
      sb.append("\"timestamp:\"");
      sb.append(timestamp);
      sb.append(",\"value:\"");
    }
    sb.append(value);
    if (!simple) {
      sb.append('}');
    }
  }

}
