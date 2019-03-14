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
import net.eiroca.library.metrics.datum.IDatum;

public class DeltaMeasure extends SnappedMeasure {

  public Measure observed;

  public DeltaMeasure(final Measure observed) {
    super(observed);
  }

  public DeltaMeasure(final MetricMetadata metadata, final Measure observed) {
    super(metadata, observed);
  }

  @Override
  protected void update(final IDatum dest, final IDatum newDatum, final IDatum oldDatum) {
    dest.setValue(newDatum.getValue() - oldDatum.getValue());
  }

}
