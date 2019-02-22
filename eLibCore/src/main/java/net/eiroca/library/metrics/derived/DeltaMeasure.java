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

import java.util.Map;
import net.eiroca.library.metrics.Datum;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureMetadata;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.metrics.SplittedDatum;
import net.eiroca.library.metrics.util.MeasureSnapshot;
import net.eiroca.library.metrics.util.SnapshotStorage;

public class DeltaMeasure extends Measure implements IDerivedMeasure {

  public Measure observed;

  public DeltaMeasure(final MeasureGroup mg, final String name, final Measure observed) {
    super(mg, new MeasureMetadata(name, mg.getMeasureNameFormat(), 0));
    this.observed = observed;
  }

  @Override
  public void reset() {
  }

  @Override
  public void refresh() {
    super.reset();
    final MeasureSnapshot oldSnap = SnapshotStorage.get(id);
    SnapshotStorage.put(id, new MeasureSnapshot(observed));
    if (oldSnap == null) { return; }
    setValue(observed.getValue() - oldSnap.datum.value);
    if (observed.hasSplittings()) {
      for (final MeasureSplitting ms : observed.getSplittings()) {
        final String splitName = ms.getName();
        final Map<String, Datum> split = oldSnap.splittings.get(splitName);
        if (split != null) {
          final MeasureSplitting dms = getSplitting(splitName);
          for (final SplittedDatum mm : ms.getSplitings()) {
            final String splitKey = mm.getName();
            final Datum old = split.get(splitKey);
            if (old != null) {
              dms.setValue(splitKey, mm.getValue() - old.value);
            }
          }
        }
      }
    }
  }

}
