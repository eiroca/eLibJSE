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

public class SimpleMeasure {

  private static final double VALUE_TRUE = 1.0;
  private static final double VALUE_FALSE = 0.0;

  String name;
  double defValue;
  //
  Datum datum = new Datum();

  public SimpleMeasure() {
    this(null, 0.0);
  }

  public SimpleMeasure(final String name) {
    this(name, 0.0);
  }

  public SimpleMeasure(final String name, final double defValue) {
    this.name = name;
    this.defValue = defValue;
    reset();
  }

  public void reset() {
    datum.value = defValue;
    datum.timeStamp = 0;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append('"').append(name).append("\":").append(datum.value);
    return sb.toString();
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public double getDefValue() {
    return defValue;
  }

  public void setDefValue(final double defValue) {
    this.defValue = defValue;
  }

  public double getValue() {
    return datum.value;
  }

  public void setValue(final boolean value) {
    setValue(value ? SimpleMeasure.VALUE_TRUE : SimpleMeasure.VALUE_FALSE);
  }

  public void setValue(final double value) {
    this.datum.value = value;
    datum.timeStamp = System.nanoTime();
  }

  public void addValue(final double value) {
    if (datum.timeStamp == 0) datum.timeStamp = System.nanoTime();
    this.datum.value += value;
  }

  public long getTimeStamp() {
    return datum.timeStamp;
  }

  public boolean hasValue() {
    return datum.timeStamp != 0;
  }

}
