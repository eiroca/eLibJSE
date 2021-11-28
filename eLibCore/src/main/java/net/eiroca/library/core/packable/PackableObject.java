/**
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
 **/
package net.eiroca.library.core.packable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.eiroca.library.core.LibReflection;
import net.eiroca.library.core.LibStr;

abstract public class PackableObject implements IPackable {

  public PackableObject() {
    expand();
  }

  @Override
  public void compact() {
    final Map<String, Field> fields = LibReflection.getFieldMap(this, true);
    for (final Field f : fields.values()) {
      try {
        final Class<?> c = f.getType();
        if (c == String.class) {
          String v;
          v = (String)f.get(this);
          if (LibStr.isEmptyOrNull(v)) {
            f.set(this, null);
          }
        }
        else if (c.isAssignableFrom(List.class)) {
          List<?> l;
          l = (List<?>)f.get(this);
          if (l.size() == 0) {
            f.set(this, null);
          }
        }
        else if (c.isAssignableFrom(Map.class)) {
          Map<?, ?> m;
          m = (Map<?, ?>)f.get(this);
          if (m.size() == 0) {
            f.set(this, null);
          }
        }
        else if (c.isAssignableFrom(Set.class)) {
          Set<?> s;
          s = (Set<?>)f.get(this);
          if (s.size() == 0) {
            f.set(this, null);
          }
        }
        else {
          final Object o = f.get(this);
          if (o instanceof IPackable) {
            ((IPackable)o).compact();
            if (((IPackable)o).isEmpty()) {
              f.set(this, null);
            }
          }
        }
      }
      catch (IllegalArgumentException | IllegalAccessException e) {
      }
    }
  }

  @Override
  public boolean isEmpty() {
    boolean isNull = true;
    final Map<String, Field> fields = LibReflection.getFieldMap(this, true);
    for (final Field f : fields.values()) {
      Object o;
      try {
        o = f.get(this);
        if (o != null) {
          isNull = false;
          break;
        }
      }
      catch (IllegalArgumentException | IllegalAccessException e) {
      }
    }
    return isNull;
  }

}
