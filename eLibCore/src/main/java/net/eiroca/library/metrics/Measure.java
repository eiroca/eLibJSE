/**
 *
 * Copyright (C) 1999-2019 Enrico Croce - AGPL >= 3.0
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

import net.eiroca.library.metrics.datum.Datum;

public class Measure extends Metric<Measure, Datum> {

  private static final double VALUE_TRUE = 1.0;
  private static final double VALUE_FALSE = 0.0;

  public Measure() {
    this((MetricMetadata)null);
  }

  public Measure(final String name) {
    this(new MetricMetadata(name));
  }

  public Measure(final MetricMetadata metadata) {
    this.metadata = metadata;
    datum = newDatum();
  }

  @Override
  public Datum newDatum() {
    return new Datum();
  }

  @Override
  public Measure newSplit(final String name) {
    return new Measure(metadata);
  }

  public void setValue(final boolean value) {
    setValue(value ? Measure.VALUE_TRUE : Measure.VALUE_FALSE);
  }

  @Override
  public long getTimeStamp() {
    return datum.getTimeStamp();
  }

}
