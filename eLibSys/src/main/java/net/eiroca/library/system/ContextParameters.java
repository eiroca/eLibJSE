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
package net.eiroca.library.system;

import java.util.HashMap;
import java.util.Map;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.parameter.BooleanParameter;
import net.eiroca.library.parameter.IntegerParameter;
import net.eiroca.library.parameter.LongParameter;
import net.eiroca.library.parameter.Parameter;
import net.eiroca.library.parameter.Parameters;
import net.eiroca.library.parameter.PasswordParameter;
import net.eiroca.library.parameter.StringParameter;

public class ContextParameters extends Parameters {

  private static Map<Class<? extends Parameter<?>>, Integer> typeMapping = new HashMap<>();
  static {
    ContextParameters.typeMapping.put(StringParameter.class, 0);
    ContextParameters.typeMapping.put(IntegerParameter.class, 1);
    ContextParameters.typeMapping.put(LongParameter.class, 2);
    ContextParameters.typeMapping.put(BooleanParameter.class, 3);
    ContextParameters.typeMapping.put(PasswordParameter.class, 4);
  }

  public ContextParameters() {
  }

  public void laodConfig(final IContext context, final String prefix) throws IllegalArgumentException {
    context.debug("Loading params");
    for (final Parameter<?> p : params) {
      final String key = LibStr.concatenate(prefix, p.getName());
      final boolean present = true;
      String value = null;
      final Integer type = ContextParameters.typeMapping.get(p.getClass());
      final int typeVal = (type != null) ? type.intValue() : -1;
      switch (typeVal) {
        case 0:
          value = String.valueOf(context.getConfigString(key, ((StringParameter)p).getDefault()));
          break;
        case 1:
          value = String.valueOf(context.getConfigInt(key, ((IntegerParameter)p).getDefault()));
          break;
        case 2:
          value = String.valueOf(context.getConfigLong(key, ((LongParameter)p).getDefault()));
          break;
        case 3:
          value = String.valueOf(context.getConfigBoolean(key, ((BooleanParameter)p).getDefault()));
          break;
        case 4:
          value = String.valueOf(context.getConfigPassword(key));
          break;
        default:
          value = context.getConfigString(key, null);
          break;
      }
      final boolean isNull = LibStr.isEmptyOrNull(value);
      if (present && !p.isNullable() && isNull) { throw new IllegalArgumentException("Parameter '" + key + "' may not be null"); }
      p.formString(value);
    }
  }

}