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
package net.eiroca.library.metrics.derived;

import java.util.concurrent.TimeUnit;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricMetadata;
import net.eiroca.library.metrics.datum.Datum;

public class RateMeasure extends SnappedMeasure {

  private static final double ZERO = 0.00001;

  private final TimeUnit timeUnit;
  private final Double minRate;

  public RateMeasure(final Measure observed, final TimeUnit timeUnit, final Double minRate) {
    this(null, observed, timeUnit, minRate);
  }

  public RateMeasure(final MetricMetadata metadata, final Measure observed, final TimeUnit timeUnit, final Double minRate) {
    super(metadata, observed);
    this.timeUnit = timeUnit;
    this.minRate = minRate;
  }

  @Override
  protected void update(final Datum dest, final Datum newDatum, final Datum oldDatum) {
    final long duration = newDatum.getTimeStamp() - oldDatum.getTimeStamp();
    final double diff = newDatum.getValue() - oldDatum.getValue();
    final double timeDivisor = ((double)duration) / timeUnit.toMillis(1);
    double rate = 0;
    if (timeDivisor > RateMeasure.ZERO) {
      rate = diff / timeDivisor;
      if ((minRate != null) && (rate < minRate)) {
        rate = minRate;
      }
      dest.setValue(rate);
    }
    else {
      // No rate
    }
  }

}
