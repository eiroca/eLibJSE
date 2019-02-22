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
package net.eiroca.library.metrics.util;

import java.util.HashMap;
import java.util.Map;
import net.eiroca.library.metrics.Datum;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.metrics.SimpleMeasure;

public class MeasureSnapshot {

  public Datum datum;
  public Map<String, Map<String, Datum>> splittings = new HashMap<>();

  public MeasureSnapshot(final Measure m) {
    datum = new Datum(m);
    if (m.hasSplittings()) {
      for (final MeasureSplitting ms : m.getSplittings()) {
        final Map<String, Datum> snap = splittings.put(ms.getName(), new HashMap<String, Datum>());
        for (final SimpleMeasure mm : ms.getSplits()) {
          snap.put(mm.getName(), new Datum(mm));
        }
      }
    }
  }

}
