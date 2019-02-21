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

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class LibFormat {

  private static final ThreadLocal<HashMap<String, SimpleDateFormat>> simpleDateFormatCache = new ThreadLocal<HashMap<String, SimpleDateFormat>>() {

    @Override
    protected HashMap<String, SimpleDateFormat> initialValue() {
      return new HashMap<>();
    }
  };

  public static SimpleDateFormat getSimpleDateFormat(final String string) {
    final HashMap<String, SimpleDateFormat> localCache = LibFormat.simpleDateFormatCache.get();
    SimpleDateFormat simpleDateFormat = localCache.get(string);
    if (simpleDateFormat == null) {
      simpleDateFormat = new SimpleDateFormat(string);
      localCache.put(string, simpleDateFormat);
      LibFormat.simpleDateFormatCache.set(localCache);
    }
    return simpleDateFormat;
  }

}
