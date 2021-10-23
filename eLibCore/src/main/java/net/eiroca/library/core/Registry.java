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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Registry<T> {

  private final String defaultName = "*";
  private final HashMap<String, T> names = new HashMap<>();

  public void addEntry(final T obj) {
    names.put(defaultName.toLowerCase(), obj);
  }

  public void addEntry(final String name, final T obj) {
    names.put(name.toLowerCase(), obj);
  }

  public List<String> getNames() {
    final List<String> result = new ArrayList<>();
    for (final String x : names.keySet()) {
      result.add(x);
    }
    return result;
  }

  public String defaultName() {
    return defaultName;
  }

  public boolean isValid(final String name) {
    return (name != null) ? names.get(name.toLowerCase()) != null : false;
  }

  public String value(String name) {
    if (name == null) {
      name = defaultName();
    }
    final T obj = names.get(name.toLowerCase());
    if (obj == null) { return name; }
    return obj.toString();
  }

  public T get(String name) {
    if (name == null) {
      name = defaultName();
    }
    return names.get(name.toLowerCase());
  }

}
