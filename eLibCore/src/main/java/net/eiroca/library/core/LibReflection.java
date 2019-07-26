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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibReflection {

  public static List<Field> getFields(final Object o, final boolean superClasses) {
    final List<Field> result = new ArrayList<>();
    final Class<?> baseClass = o.getClass();
    for (Class<?> c = baseClass; c != null; c = c.getSuperclass()) {
      final Field[] fields = c.getDeclaredFields();
      for (final Field classField : fields) {
        result.add(classField);
      }
      if (!superClasses) {
        break;
      }
    }
    return result;
  }

  public static Map<String, Field> getFieldMap(final Object o, final boolean superClasses) {
    final Map<String, Field> result = new HashMap<>();
    final Class<?> baseClass = o.getClass();
    for (Class<?> c = baseClass; c != null; c = c.getSuperclass()) {
      final Field[] fields = c.getDeclaredFields();
      for (final Field classField : fields) {
        final String name = classField.getName();
        if (!result.containsKey(name)) {
          result.put(name, classField);
        }
      }
      if (!superClasses) {
        break;
      }
    }
    return result;
  }

}
