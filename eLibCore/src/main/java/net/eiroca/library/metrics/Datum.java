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

public class Datum {

  public long timeStamp;
  public double value;

  public Datum() {
    value = 0;
    timeStamp = 0;
  }

  public Datum(final Datum m) {
    if (m.hasValue()) {
      timeStamp = m.getTimeStamp();
      value = m.getValue();
    }
    else {
      timeStamp = System.currentTimeMillis();
      value = 0;
    }
  }

  public void reset() {
    timeStamp = 0;
  }

  public void reset(final double defVal) {
    timeStamp = 0;
    value = defVal;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(value);
    return sb.toString();
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public boolean hasValue() {
    return timeStamp != 0;
  }

  public double getValue() {
    return value;
  }

  public void setValue(final double value) {
    this.value = value;
    timeStamp = System.currentTimeMillis();
  }

  public void addValue(final Datum datum) {
    if (timeStamp == 0) {
      timeStamp = datum.timeStamp != 0 ? datum.timeStamp : System.currentTimeMillis();
    }
    this.value += datum.value;
  }

  public void addValue(final double value) {
    if (timeStamp == 0) {
      timeStamp = System.currentTimeMillis();
    }
    this.value += value;
  }

}