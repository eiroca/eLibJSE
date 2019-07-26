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

public class LongParameter extends Parameter<Long> {

  public long minVal = Long.MIN_VALUE;
  public long maxVal = Long.MAX_VALUE;

  public LongParameter(final Parameters owner, final String paramName, final long defValue, final long minVal, final long maxVal) {
    super(owner, paramName, defValue, true, false);
    this.minVal = minVal;
    this.maxVal = maxVal;
  }

  public LongParameter(final Parameters owner, final String paramName, final long paramDef, final boolean required, final boolean nullable) {
    super(owner, paramName, paramDef, required, nullable);

  }

  public LongParameter(final Parameters owner, final String paramName, final long paramDef) {
    super(owner, paramName, paramDef);
  }

  public LongParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  @Override
  public Long convertString(final String strValue) {
    Long value;
    if (LibStr.isEmptyOrNull(strValue)) {
      value = getDefault();
    }
    else {
      try {
        value = new Long(strValue.trim());
      }
      catch (final NumberFormatException e) {
        value = getDefault();
      }
    }
    return value;
  }

  @Override
  public boolean isValid(final Object value) {
    return value instanceof Long;
  }

}
