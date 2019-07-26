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
package net.eiroca.library.metrics.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.datum.Datum;

public class MeasureSnapshot {

  public Datum datum;
  public Map<String, MeasureSnapshot> splittings = null;

  public MeasureSnapshot(final IMetric<?> m) {
    datum = new Datum(m.getDatum());
    if (m.hasSplittings()) {
      splittings = new HashMap<>();
      for (final Entry<String, ?> ms : m.getSplittings().entrySet()) {
        final String splitName = ms.getKey();
        final IMetric<?> split = (IMetric<?>)ms.getValue();
        splittings.put(splitName, new MeasureSnapshot(split));
      }
    }
  }

  public boolean hasSplittings() {
    return (splittings != null) && (splittings.size() > 0);
  }

}
