/**
 * Copyright (C) 1999-2026 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.data;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class DynamicData {

  public SortedMap<String, Object> data = null;

  protected transient static Set<String> skipped = new HashSet<>();
  static {
    DynamicData.skipped.add("data");
  }

  protected transient SortedMap<String, MethodHandle> setters = new TreeMap<>();
  protected transient SortedMap<String, MethodHandle> getters = new TreeMap<>();
  protected transient SortedMap<String, Field> maps = new TreeMap<>();
  protected transient SortedMap<String, Field> lists = new TreeMap<>();

  public DynamicData() {
    final Lookup lookup = MethodHandles.lookup();
    Class<?> current = this.getClass();
    while (current != null) {
      for (final Field field : current.getDeclaredFields()) {
        final boolean isTransient = Modifier.isTransient(field.getModifiers());
        if (isTransient || DynamicData.skipped.contains(field.getName())) {
          continue;
        }
        final String name = field.getName();
        final Class<?> type = field.getType();
        if (Map.class.isAssignableFrom(type)) {
          maps.put(name, field);
        }
        else if (type.isAssignableFrom(List.class)) {
          lists.put(name, field);
        }
        else {
          try {
            getters.put(name, lookup.unreflectGetter(field));
          }
          catch (final IllegalAccessException e) {
          }
          try {
            setters.put(name, lookup.unreflectSetter(field));
          }
          catch (final IllegalAccessException e) {
          }
        }
      }
      current = current.getSuperclass();
    }
  }

  public <T> void set(final String key, final T val) {
    boolean isExt = true;
    final MethodHandle setter = setters.get(key);
    if (setter != null) {
      try {
        setter.invoke(this, val);
        isExt = false;
      }
      catch (final Throwable e) {
      }
    }
    if (isExt) {
      if (val != null) {
        if (data == null) {
          data = new TreeMap<>();
        }
        data.put(key, val);
      }
      else {
        if (data != null) {
          data.remove(key);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T get(final String key, final T def) {
    boolean isExt = true;
    T result = null;
    final MethodHandle getter = getters.get(key);
    if (getter != null) {
      try {
        result = (T)getter.invoke(this);
        isExt = false;
      }
      catch (final Throwable e) {
      }
    }
    if ((isExt) && (data != null)) {
      if (data.containsKey(key)) {
        result = (T)data.get(key);
      }
    }
    return (result != null) ? result : def;
  }

  @SuppressWarnings("unchecked")
  public void assign(final DynamicData src) throws Exception {
    if (src != null) {
      for (final Entry<String, MethodHandle> x : src.getters.entrySet()) {
        final String name = x.getKey();
        final MethodHandle getter = x.getValue();
        try {
          set(name, getter.invoke(src));
        }
        catch (final Throwable e) {
        }
      }
      for (final Entry<String, Field> x : maps.entrySet()) {
        final String name = x.getKey();
        final Field dstMap = x.getValue();
        final Field srcMap = src.maps.get(name);
        if ((srcMap != null) && (dstMap != null)) {
          Map<String, DynamicData> d = null;
          Map<String, DynamicData> s = null;
          try {
            d = (Map<String, DynamicData>)dstMap.get(this);
            s = (Map<String, DynamicData>)srcMap.get(src);
          }
          catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
          }
          if ((s != null) && (d != null)) {
            for (final Entry<String, DynamicData> y : s.entrySet()) {
              final String key = y.getKey();
              final DynamicData val = y.getValue();
              d.put(key, val);
            }
          }
        }
      }
      if (src.data != null) {
        if (data == null) {
          data = new TreeMap<>();
        }
        data.putAll(src.data);
      }
    }
  }

  public void removeExt(final String key) {
    if (data != null) {
      data.remove(key);
      if (data.size() == 0) {
        data = null;
      }
    }
  }

}
