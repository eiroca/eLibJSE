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
import java.util.UUID;
import net.eiroca.library.metrics.Datum;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.metrics.SimpleMeasure;
import net.eiroca.library.metrics.util.MeasureSnapshot;
import net.eiroca.library.metrics.util.SnapshotStorage;

public class GrowMeasure extends Measure implements IDerivedMeasure {

  private static final double ZERO = 0.00001;

  private final UUID id = UUID.randomUUID();
  private final Measure observed;

  public GrowMeasure(final MeasureGroup mg, final String name, final Measure observed) {
    super(mg, name);
    this.observed = observed;
  }

  @Override
  public void refresh() {
    super.reset();
    MeasureSnapshot snap = SnapshotStorage.get(id);
    SnapshotStorage.put(id, new MeasureSnapshot(observed));
    if (snap == null) return;
    double diff = observed.getValue() - snap.datum.value;
    double base = snap.datum.value;
    double rate = 0;
    if (base > ZERO) {
      rate = diff / base - 1;
      setValue(rate);
    }
    if (observed.hasSplittings()) {
      for (MeasureSplitting ms : observed.getSplittings()) {
        String splitName = ms.getName();
        Map<String, Datum> split = snap.splittings.get(splitName);
        if (split != null) {
          MeasureSplitting dms = getSplitting(splitName);
          for (SimpleMeasure mm : ms.getSplits()) {
            String splitKey = mm.getName();
            Datum old = split.get(splitKey);
            if (old != null) {
              diff = mm.getValue() - old.value;
              base = old.value;
              if (base > ZERO) {
                rate = diff / base - 1;
                dms.setValue(splitKey, rate);
              }
            }
          }
        }
      }
    }
  }

}
