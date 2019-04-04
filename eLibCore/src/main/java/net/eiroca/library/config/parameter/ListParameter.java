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
package net.eiroca.library.config.parameter;

import net.eiroca.library.config.Parameter;
import net.eiroca.library.config.Parameters;
import net.eiroca.library.core.LibStr;

public class ListParameter extends Parameter<String[]> {

  String separator = "[\\s\\t]+";

  public ListParameter(final Parameters owner, final String paramName, final String separator, final String paramDef, final boolean required, final boolean nullable) {
    super(owner, paramName, null, required, nullable);
    this.separator = separator;
    defValue = (paramDef != null) ? paramDef.split(separator) : null;
  }

  public ListParameter(final Parameters owner, final String paramName, final String separator, final String paramDef) {
    super(owner, paramName, null);
    this.separator = separator;
    defValue = paramDef.split(separator);
  }

  public ListParameter(final Parameters owner, final String paramName, final String[] paramDef) {
    super(owner, paramName, paramDef);
  }

  public ListParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(final String separator) {
    this.separator = separator;
  }

  @Override
  public String[] convertString(final String strValue) {
    String[] value;
    if (LibStr.isEmptyOrNull(strValue)) {
      value = defValue;
    }
    else {
      value = strValue.split(separator);
    }
    return value;
  }

  @Override
  public String encodeString(final Object val) {
    return LibStr.merge((String[])val, separator, "");
  }

  @Override
  public boolean isValid(final Object value) {
    return value instanceof String[];
  }

}