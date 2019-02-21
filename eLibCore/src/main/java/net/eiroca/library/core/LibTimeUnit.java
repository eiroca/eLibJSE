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
package net.eiroca.library.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LibTimeUnit {

  private static Map<Character, TimeUnit> TIMEUNITS = new HashMap<>();
  static {
    LibTimeUnit.TIMEUNITS.put('s', TimeUnit.SECONDS);
    LibTimeUnit.TIMEUNITS.put('m', TimeUnit.MINUTES);
    LibTimeUnit.TIMEUNITS.put('h', TimeUnit.HOURS);
    LibTimeUnit.TIMEUNITS.put('d', TimeUnit.DAYS);
    LibTimeUnit.TIMEUNITS.put('n', TimeUnit.NANOSECONDS);
  }

  public static long getFrequency(String val, final int defFreq, final TimeUnit seconds, final int minFreq, final int maxFreq) {
    if (LibStr.isEmptyOrNull(val)) { return defFreq; }
    TimeUnit unit = TimeUnit.MILLISECONDS;
    final char timeUnit = val.charAt(val.length() - 1);
    if (!Character.isDigit(timeUnit)) {
      val = val.substring(0, val.length() - 1);
      if (LibTimeUnit.TIMEUNITS.containsKey(timeUnit)) {
        unit = LibTimeUnit.TIMEUNITS.get(timeUnit);
      }
    }
    long freq = Helper.getLong(val, -1);
    if (freq < 0) { return defFreq; }
    freq = seconds.convert(freq, unit);
    if (freq < minFreq) {
      freq = minFreq;
    }
    else if (freq > maxFreq) {
      freq = maxFreq;
    }
    return freq;
  }

}
