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

import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricMetadata;
import net.eiroca.library.metrics.datum.Datum;

public class HitMissRatioMeasure extends RatioMeasure {

  public HitMissRatioMeasure(final Measure hitMeasure, final Measure missMeasure) {
    super(hitMeasure, missMeasure);
  }

  public HitMissRatioMeasure(final MetricMetadata metadata, final Measure hitMeasure, final Measure missMeasure) {
    super(metadata, hitMeasure, missMeasure);
  }

  @Override
  protected void update(final Datum dest, final double num, final double den) {
    final double tot = den + num;
    if (tot > RatioMeasure.ZERO) {
      dest.setValue(num / tot);
    }
  }

}
