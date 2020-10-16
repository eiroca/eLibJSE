/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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

import net.eiroca.library.data.Tags;
import net.eiroca.library.metrics.datum.Datum;

public class Measure extends Metric<Measure, Datum> {

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
    final Measure m = new Measure();
    m.parent = this;
    return m;
  }

  @Override
  public long getTimeStamp() {
    return datum.getTimeStamp();
  }

  public Measure dimensions(final String... splitNames) {
    final Measure result = this;
    if (metadata != null) {
      Tags dimensions = metadata.dimensions();
      for (final String splitName : splitNames) {
        dimensions.add(splitName);
      }
    }
    return result;
  }

}
