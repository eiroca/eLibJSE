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
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureMetadata;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.metrics.SplittedDatum;

public class RatioMeasure extends Measure implements IDerivedMeasure {

  private static final double ZERO = 0.00001;
  public Measure numMeasure;
  public Measure denMeasure;

  public RatioMeasure(final MeasureGroup mg, final String name, final Measure numMeasure, final Measure denMeasure) {
    super(mg, new MeasureMetadata(name, mg.getMeasureNameFormat(), 0));
    this.numMeasure = numMeasure;
    this.denMeasure = denMeasure;
  }

  @Override
  public void refresh() {
    reset();
    if (denMeasure.hasValue()) {
      final double num = numMeasure.getValue();
      final double den = denMeasure.getValue();
      if (den > RatioMeasure.ZERO) {
        setValue(num / den);
      }
    }
    if (denMeasure.hasSplittings()) {
      for (final MeasureSplitting denSplit : denMeasure.getSplittings()) {
        final String splitGroup = denSplit.getName();
        final MeasureSplitting nums = numMeasure.getSplitting(splitGroup);
        final MeasureSplitting dens = denMeasure.getSplitting(splitGroup);
        final MeasureSplitting result = getSplitting(splitGroup);
        for (final SplittedDatum split : denSplit.getSplitings()) {
          final String splitName = split.getName();
          final double num = nums.getValue(splitName, 0);
          final double den = dens.getValue(splitName, 0);
          if (den > RatioMeasure.ZERO) {
            result.setValue(splitName, num / den);
          }
        }
      }
    }
  }

}
