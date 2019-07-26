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
package net.eiroca.library.metrics.derived;

import java.util.Map.Entry;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricMetadata;
import net.eiroca.library.metrics.datum.Datum;
import net.eiroca.library.metrics.datum.IDatum;

public class RatioMeasure extends Measure implements IDerivedMeasure {

  protected static final double ZERO = 0.00001;
  public Measure numMeasure;
  public Measure denMeasure;

  public RatioMeasure(final Measure numMeasure, final Measure denMeasure) {
    this(null, numMeasure, denMeasure);
  }

  public RatioMeasure(final MetricMetadata metadata, final Measure numMeasure, final Measure denMeasure) {
    super(metadata);
    this.numMeasure = numMeasure;
    this.denMeasure = denMeasure;
  }

  @Override
  public void refresh() {
    reset();
    iterate(this, numMeasure, denMeasure);
  }

  private void iterate(final IMetric<?> ratioMeasure, final IMetric<?> num, final IMetric<?> den) {
    if (denMeasure.hasValue()) {
      update(getDatum(), numMeasure.getValue(), denMeasure.getValue());
    }
    if (denMeasure.hasSplittings()) {
      for (final Entry<String, IMetric<Datum>> ms : denMeasure.getSplittings().entrySet()) {
        final String splitGroup = ms.getKey();
        final IMetric<?> dens = ms.getValue();
        final IMetric<?> nums = numMeasure.getSplitting(splitGroup);
        final IMetric<?> result = getSplitting(splitGroup);
        update(result.getDatum(), nums.getValue(), dens.getValue());
        if (denMeasure.hasSplittings()) {
          iterate(result, nums, dens);
        }
      }
    }
  }

  protected void update(final IDatum dest, final double num, final double den) {
    if (den > RatioMeasure.ZERO) {
      dest.setValue(num / den);
    }
  }

}
