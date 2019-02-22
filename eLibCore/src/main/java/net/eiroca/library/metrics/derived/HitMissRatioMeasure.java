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

public class HitMissRatioMeasure extends Measure implements IDerivedMeasure {

  private static final double ZERO = 0.00001;
  public Measure hitMeasure;
  public Measure missMeasure;

  public HitMissRatioMeasure(final MeasureGroup mg, final String name, final Measure hitMeasure, final Measure missMeasure) {
    super(mg, new MeasureMetadata(name, mg.getMeasureNameFormat(), 0));
    this.hitMeasure = hitMeasure;
    this.missMeasure = missMeasure;
  }

  @Override
  public void refresh() {
    reset();
    if (hitMeasure.hasValue()) {
      final double num = hitMeasure.getValue();
      final double den = missMeasure.getValue() + num;
      if (den > HitMissRatioMeasure.ZERO) {
        setValue(num / den);
      }
    }
    if (hitMeasure.hasSplittings()) {
      for (final MeasureSplitting hitSplit : hitMeasure.getSplittings()) {
        final String splitGroup = hitSplit.getName();
        final MeasureSplitting result = getSplitting(splitGroup);
        final MeasureSplitting denoms = missMeasure.getSplitting(splitGroup);
        for (final SplittedDatum split : hitSplit.getSplitings()) {
          final String splitName = split.getName();
          final double num = split.getValue();
          final double den = denoms.getValue(splitName, 0) + num;
          if (den > HitMissRatioMeasure.ZERO) {
            result.setValue(splitName, num / den);
          }
        }
      }
    }
  }

}
