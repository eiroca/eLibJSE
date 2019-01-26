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
import java.util.concurrent.TimeUnit;
import net.eiroca.library.metrics.Datum;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.metrics.SimpleMeasure;
import net.eiroca.library.metrics.util.MeasureSnapshot;
import net.eiroca.library.metrics.util.SnapshotStorage;

public class RateMeasure extends Measure implements IDerivedMeasure {

  private static final double ZERO = 0.00001;

  private final UUID id = UUID.randomUUID();
  private final Measure observed;
  private final TimeUnit timeUnit;
  private final Double minRate;

  public RateMeasure(final MeasureGroup mg, final String name, final Measure observed, TimeUnit timeUnit, Double minRate) {
    super(mg, name);
    this.observed = observed;
    this.timeUnit = timeUnit;
    this.minRate = minRate;
  }

  @Override
  public void refresh() {
    super.reset();
    MeasureSnapshot oldSnap = SnapshotStorage.get(id);
    SnapshotStorage.put(id, new MeasureSnapshot(observed));
    if (oldSnap == null) return;
    long duration = observed.getTimeStamp() - oldSnap.datum.timeStamp;
    double diff = observed.getValue() - oldSnap.datum.value;
    double timeDivisor = ((double)duration) / timeUnit.toMillis(1);
    double rate = 0;
    if (timeDivisor > ZERO) {
      rate = diff / timeDivisor;
      if ((minRate != null) && (rate < minRate)) rate = minRate;
      setValue(rate);
      if (observed.hasSplittings()) {
        for (MeasureSplitting ms : observed.getSplittings()) {
          String splitName = ms.getName();
          Map<String, Datum> split = oldSnap.splittings.get(splitName);
          if (split != null) {
            MeasureSplitting dms = getSplitting(splitName);
            for (SimpleMeasure mm : ms.getSplits()) {
              String splitKey = mm.getName();
              Datum old = split.get(splitKey);
              if (old != null) {
                diff = mm.getValue() - old.value;
                rate = diff / timeDivisor;
                if ((minRate != null) && (rate < minRate)) rate = minRate;
                dms.setValue(splitKey, rate);
              }
            }
          }
        }
      }
    }
  }

}
