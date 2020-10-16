/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.data;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortedProperties extends Properties {

  private static final long serialVersionUID = -3151007061246894729L;
  private final SortedMap<Object, Object> properties = new TreeMap<>();

  @Override
  public Object get(final Object key) {
    return properties.get(key);
  }

  @Override
  public Object put(final Object key, final Object value) {
    return properties.put(key, value);
  }

  @Override
  public Object remove(final Object key) {
    return properties.remove(key);
  }

  @Override
  public void clear() {
    properties.clear();
  }

  @Override
  public Enumeration<Object> keys() {
    return Collections.enumeration(properties.keySet());
  }

  @Override
  public Enumeration<Object> elements() {
    return Collections.enumeration(properties.values());
  }

  @Override
  public Set<Map.Entry<Object, Object>> entrySet() {
    return properties.entrySet();
  }

  @Override
  public int size() {
    return properties.size();
  }

  @Override
  public String getProperty(final String key) {
    return (String)properties.get(key);
  }

  @Override
  public synchronized boolean containsKey(final Object key) {
    return properties.containsKey(key);
  }

  @Override
  public String toString() {
    return properties.toString();
  }

}
