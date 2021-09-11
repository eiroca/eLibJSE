/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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
import java.util.ArrayList;
import java.util.HashMap;
import net.eiroca.library.data.Pair;

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

  private static ArrayList<Pair<String, Double>> MODIFIERSTR = new ArrayList<>();
  static {
    LibFormat.MODIFIERSTR.add(new Pair<>("%", 1.0 / 100.0));
    LibFormat.MODIFIERSTR.add(new Pair<>("ns", 1 / 1000.0));
    LibFormat.MODIFIERSTR.add(new Pair<>("ms", 1.0));
    LibFormat.MODIFIERSTR.add(new Pair<>("s", 1000.0));
    LibFormat.MODIFIERSTR.add(new Pair<>("\"", 1000.0));
    LibFormat.MODIFIERSTR.add(new Pair<>("'", 60000.0));
    LibFormat.MODIFIERSTR.add(new Pair<>("m", 60000.0));
    LibFormat.MODIFIERSTR.add(new Pair<>("h", 3600000.0));
  }

  public static HashMap<String, Double> STRVALUE = new HashMap<>();
  static {
    LibFormat.STRVALUE.put("true", 1.0);
    LibFormat.STRVALUE.put("false", 0.0);
    LibFormat.STRVALUE.put("ok", 0.0);
    LibFormat.STRVALUE.put("ko", 1.0);
    LibFormat.STRVALUE.put("on", 1.0);
    LibFormat.STRVALUE.put("off", 0.0);
    //
    LibFormat.STRVALUE.put("panic", 1.0);
    LibFormat.STRVALUE.put("fatal", 1.0);
    LibFormat.STRVALUE.put("critical", 0.95);
    LibFormat.STRVALUE.put("severe", .95);
    LibFormat.STRVALUE.put("error", 0.9);
    LibFormat.STRVALUE.put("warn", 0.5);
    LibFormat.STRVALUE.put("warning", 0.5);
    LibFormat.STRVALUE.put("info", 0.1);
  }

  public static final Double getValue(String value) {
    if (value == null) { return null; }
    value = value.trim().toLowerCase();
    boolean negated = false;
    double modifier = 1.0;
    Double val = null;
    if (value.endsWith("!")) {
      negated = true;
      value = value.substring(0, value.length() - 1);
    }
    for (final String strVal : LibFormat.STRVALUE.keySet()) {
      if (value.startsWith(strVal)) {
        val = LibFormat.STRVALUE.get(strVal);
        value = value.substring(strVal.length());
        break;
      }
    }
    for (final Pair<String, Double> strVal : LibFormat.MODIFIERSTR) {
      if (value.endsWith(strVal.getLeft())) {
        modifier = strVal.getRight().doubleValue();
        value = value.substring(0, value.length() - strVal.getLeft().length());
        break;
      }
    }
    if (val == null) {
      try {
        val = new Double(Double.parseDouble(value) * modifier);
      }
      catch (final NumberFormatException e) {
      }
    }
    if (negated) {
      if (Math.abs(val) < 0.000001) {
        val = 1.0;
      }
      else {
        val = 0.0;
      }
    }
    return val;
  }

}
