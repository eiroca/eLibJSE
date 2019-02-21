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
package net.eiroca.library.parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.eiroca.library.core.LibStr;

public class Parameters {

  protected List<Parameter<?>> params = new ArrayList<>();

  public Parameters() {
  }

  public void add(final Parameter<?> p) {
    params.add(p);
  }

  public int count() {
    return params.size();
  }

  public void laodConfig(final Map<String, String> config, final String prefix) throws IllegalArgumentException {
    for (final Parameter<?> p : params) {
      final String key = LibStr.concatenate(prefix, p.getName());
      final boolean present = config.containsKey(key);
      final String value = config.get(key);
      final boolean isNull = LibStr.isEmptyOrNull(value);
      if (p.isRequired() && !present) { throw new IllegalArgumentException(String.format("Parameter '%s' must exist", key)); }
      if (present && !p.isNullable() && isNull) { throw new IllegalArgumentException(String.format("Parameter '%s' cannot be null", key)); }
      p.formString(value);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(256);
    sb.append("Parameters{");
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

}
