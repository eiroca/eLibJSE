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
package net.eiroca.library.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Registry {

  private final String defaultName = "*";
  private final HashMap<String, String> names = new HashMap<>();

  public void addEntry(final String classname) {
    names.put(defaultName.toLowerCase(), classname);
  }

  public void addEntry(final String name, final String classname) {
    names.put(name.toLowerCase(), classname);
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

  public String className(String name) {
    if (name == null) {
      name = defaultName();
    }
    String clazzName = names.get(name.toLowerCase());
    if (clazzName == null) {
      clazzName = name;
    }
    return clazzName;
  }

}
