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
package net.eiroca.library.diagnostics.actiondata;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.IConverter;
import net.eiroca.library.diagnostics.converters.BaseConverter;

public class ActionData {

  public static final String PARAM = "PARAM";
  public static final String COMAMND = "COMMAND";
  public static final String HOST = "HOST";
  public static final String PORT = "PORT";
  public static final String USER = "USER";

  private static final IConverter NULL_CONVERTER = new BaseConverter();

  protected final Map<String, String> data = new HashMap<>();

  public ActionData() {
    final Field[] fields = getClass().getFields();
    for (final Field f : fields) {
      f.setAccessible(true);
      final String name = f.getName();
      if (name.startsWith("_")) {
        continue;
      }
      String mapName;
      try {
        mapName = String.valueOf(f.get(null));
        if (name.startsWith("f")) {
          set(String.format(mapName, 0), (String)null);
        }
        else {
          set(mapName, (String)null);
        }
      }
      catch (IllegalArgumentException | IllegalAccessException e) {
      }
    }
  }

  public Map<String, String> getData(IConverter converter) {
    if (converter == null) {
      converter = ActionData.NULL_CONVERTER;
    }
    final Map<String, String> result = new HashMap<>();
    for (final Map.Entry<String, String> e : data.entrySet()) {
      final String key = e.getKey();
      final String val = e.getValue();
      final String valConv = converter.convert(val);
      result.put(key, valConv);
      result.put(Messages.RAWPREFIX + key, val);
    }
    return result;
  }

  public void setIF(final String name, final String value) {
    if (!data.containsKey(name)) {
      data.put(name, value);
    }
  }

  public void set(final String name, final String value) {
    data.put(name, LibStr.isNotEmptyOrNull(value) ? value : null);
  }

  public void set(final String name, final double value) {
    data.put(name, String.valueOf(value));
  }

  public void set(final String name, final boolean value) {
    data.put(name, value ? "TRUE" : "FALSE");
  }

  public void set(final String nameFormat, final String[] values) {
    set(nameFormat, values, values.length);
  }

  public void set(final String nameFormat, final String[] values, final int maxVal) {
    final int len = Math.min(maxVal, values.length);
    for (int i = 0; i < len; i++) {
      final String name = String.format(nameFormat, i);
      set(name, values[i]);
    }
    if (len < maxVal) {
      for (int i = len; i < maxVal; i++) {
        final String name = String.format(nameFormat, i);
        set(name, (String)null);
      }
    }
  }

  public void set(final String nameFormat, final List<String> values, final int maxVal) {
    final int len = Math.min(maxVal, (values != null) ? values.size() : 0);
    for (int i = 0; i < len; i++) {
      final String name = String.format(nameFormat, i);
      set(name, values.get(i));
    }
    if (len < maxVal) {
      for (int i = len; i < maxVal; i++) {
        final String name = String.format(nameFormat, i);
        set(name, (String)null);
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final Field[] fields = getClass().getFields();
    for (final Field f : fields) {
      f.setAccessible(true);
      String name = f.getName();
      if (name.startsWith("_")) {
        continue;
      }
      String mapName;
      try {
        mapName = String.valueOf(f.get(null));
        if (name.startsWith("f")) {
          for (int i = 0; i < 20; i++) {
            name = String.format(mapName, i);
            final String val = data.get(name);
            if (val == null) {
              break;
            }
            sb.append(name).append("=").append(val).append(Helper.NL);
          }
        }
        else {
          final String val = data.get(mapName);
          if (val != null) {
            sb.append(mapName).append("=").append(val).append(Helper.NL);
          }
        }
      }
      catch (IllegalArgumentException | IllegalAccessException e) {
      }
    }
    return sb.toString();
  }

}
