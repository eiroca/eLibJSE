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
package net.eiroca.library.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LibMap {

  final public static Map<String, String> getSubMap(final Map<String, String> source, final String prefix) {
    if (prefix == null) { return source; }
    final Map<String, String> result = new HashMap<>();
    for (final Entry<String, String> entry : source.entrySet()) {
      if (entry.getKey().startsWith(prefix)) {
        final String name = entry.getKey().substring(prefix.length());
        result.put(name, entry.getValue());
      }
    }
    return result;
    // return ImmutableMap.copyOf(result);
  }

  final public static Map<String, String> selectByKeys(final Map<String, String> map, final String prefix, final String[] keys) {
    final Map<String, String> result = new HashMap<>();
    for (final String key : keys) {
      final String mapKey = LibStr.concatenate(prefix, key);
      if (map.containsKey(mapKey)) {
        result.put(key, map.get(mapKey));
      }
    }
    return result;
  }

  final public static String getField(final Map<String, String> fields, final String[] fieldNames, final String sep) {
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (final String fieldName : fieldNames) {
      final String newVal = fields.get(fieldName);
      if (first) {
        Helper.concatenate(sb, newVal);
        first = false;
      }
      else {
        Helper.concatenate(sb, sep, newVal);
      }
    }
    return sb.toString();
  }

  public static Map<String, String> buildMap(final List<String> names, final List<String> values) {
    Map<String, String> result = null;
    if ((names != null) && (values != null)) {
      result = new HashMap<>(names.size() * 2);
      for (int i = 0; i < names.size(); i++) {
        final String key = names.get(i);
        final String val = values.get(i);
        result.put(key, val);
      }
    }
    else if (values != null) {
      result = new HashMap<>(values.size() * 2);
      for (int i = 0; i < values.size(); i++) {
        final String key = String.valueOf(i);
        final String val = values.get(i);
        result.put(key, val);
      }
    }
    return result;
  }

}
