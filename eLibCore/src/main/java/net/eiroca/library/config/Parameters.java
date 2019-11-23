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
package net.eiroca.library.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.eiroca.library.core.LibReflection;
import net.eiroca.library.core.LibStr;

public class Parameters {

  protected String name;
  protected List<Parameter<?>> params = new ArrayList<>();
  protected Map<Parameter<?>, Object> values = new HashMap<>();

  public Parameters() {
  }

  public Parameters(String name) {
    this.name = name;
  }

  public void add(final Parameter<?> p) {
    params.add(p);
  }

  public int count() {
    return params.size();
  }

  public void clear() {
    values.clear();
  }

  public void loadConfig(final Map<String, String> config, final String prefix) throws IllegalArgumentException {
    values.clear();
    for (final Parameter<?> p : params) {
      final String key = LibStr.concatenate(prefix, p.getName());
      final boolean present = config.containsKey(key);
      final String value = config.get(key);
      final boolean isNull = LibStr.isEmptyOrNull(value);
      if (p.isRequired() && !present) { throw new IllegalArgumentException(String.format("Parameter '%s' must exist", key)); }
      if (present && !p.isNullable() && isNull) { throw new IllegalArgumentException(String.format("Parameter '%s' cannot be null", key)); }
      final Object val = p.convertString(value);
      values.put(p, val);
    }
  }

  public void loadConfig(final Properties config, final String prefix) throws IllegalArgumentException {
    values.clear();
    for (final Parameter<?> p : params) {
      final String key = LibStr.concatenate(prefix, p.getName());
      final boolean present = config.containsKey(key);
      final String value = config.getProperty(key);
      final boolean isNull = LibStr.isEmptyOrNull(value);
      if (p.isRequired() && !present) { throw new IllegalArgumentException(String.format("Parameter '%s' must exist", key)); }
      if (present && !p.isNullable() && isNull) { throw new IllegalArgumentException(String.format("Parameter '%s' cannot be null", key)); }
      final Object val = p.convertString(value);
      values.put(p, val);
    }
  }

  public void saveConfig(final Map<String, String> config, final String prefix) {
    for (final Parameter<?> p : params) {
      final String key = LibStr.concatenate(prefix, p.getName());
      Object val = values.get(p);
      if (val == null) {
        val = p.getDefault();
      }
      if (val != null) {
        final String value = p.encodeString(val);
        config.put(key, value);
      }
    }
  }

  public void saveConfig(final IConfigurationUpdate config) {
    for (final Parameter<?> p : params) {
      final String key = p.getName();
      Object val = values.get(p);
      if (val == null) {
        val = p.getDefault();
      }
      if (val != null) {
        config.setProperty(key, val);
      }
    }
  }

  public void saveConfig(final Object config) {
    saveConfig(config, null, false, true);
  }

  public void saveConfig(final Object config, final String namePrefix, final boolean forceAccess, final boolean useSuperClass) {
    final Map<String, Field> fields = LibReflection.getFieldMap(config, useSuperClass);
    for (final Parameter<?> p : params) {
      String key = p.getInternalName();
      if (namePrefix != null) {
        key = namePrefix + key;
      }
      Object val = values.get(p);
      if (val == null) {
        val = p.getDefault();
      }
      final Field f = fields.get(key);
      if (f != null) {
        try {
          if (forceAccess) {
            f.setAccessible(true);
          }
          f.set(config, val);
        }
        catch (IllegalArgumentException | IllegalAccessException e) {
        }
      }
    }
  }

  public Object getValue(final Parameter<?> param) {
    Object v = values.get(param);
    if (v == null) {
      v = param.getDefault();
    }
    return v;
  }

  public void setValue(final Parameter<?> param, final Object value) {
    if (param.isValid(value)) {
      values.put(param, value);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(256);
    if (name != null) {
      sb.append(name).append('=');
    }
    sb.append("{");
    boolean first = true;
    for (final Parameter<?> p : params) {
      if (!first) {
        sb.append(',');
      }
      else {
        first = false;
      }
      sb.append(p);
    }
    sb.append("}");
    return sb.toString();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
