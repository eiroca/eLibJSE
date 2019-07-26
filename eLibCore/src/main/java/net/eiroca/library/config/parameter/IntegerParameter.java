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
package net.eiroca.library.config.parameter;

import net.eiroca.library.config.Parameter;
import net.eiroca.library.config.Parameters;
import net.eiroca.library.core.LibStr;

public class IntegerParameter extends Parameter<Integer> {

  public int minVal = Integer.MIN_VALUE;
  public int maxVal = Integer.MAX_VALUE;

  public IntegerParameter(final Parameters owner, final String paramName, final int defValue, final int minVal, final int maxVal) {
    super(owner, paramName, defValue, true, false);
    this.minVal = minVal;
    this.maxVal = maxVal;
  }

  public IntegerParameter(final Parameters owner, final String paramName, final int defValue, final boolean required, final boolean nullable) {
    super(owner, paramName, defValue, required, nullable);
  }

  public IntegerParameter(final Parameters owner, final String paramName, final boolean required) {
    super(owner, paramName, null, required, true);
  }

  public IntegerParameter(final Parameters owner, final String paramName, final int paramDef) {
    super(owner, paramName, paramDef);
  }

  public IntegerParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  @Override
  public Integer convertString(final String strValue) {
    Integer value;
    if (LibStr.isEmptyOrNull(strValue)) {
      value = getDefault();
    }
    else {
      try {
        value = new Integer(strValue.trim());
        if ((value < minVal) || (value > maxVal)) {
          value = getDefault();
        }
      }
      catch (final NumberFormatException e) {
        value = getDefault();
      }
    }
    return value;
  }

  @Override
  public boolean isValid(final Object value) {
    return value instanceof Integer;
  }

}
