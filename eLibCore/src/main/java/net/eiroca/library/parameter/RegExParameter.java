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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.eiroca.library.core.LibStr;

public class RegExParameter extends Parameter<Pattern> {

  private Throwable lastError = null;

  public RegExParameter(final Parameters owner, final String paramName, final String defRegExStr, final boolean required, final boolean nullable) {
    super(owner, paramName, null, required, nullable);
    defValue = decodePattern(defRegExStr);
  }

  public RegExParameter(final Parameters owner, final String paramName, final String defRegExStr) {
    super(owner, paramName, null);
    defValue = decodePattern(defRegExStr);
  }

  public RegExParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  protected Pattern decodePattern(final String regex) {
    lastError = null;
    if (regex != null) {
      try {
        return Pattern.compile(regex);
      }
      catch (final PatternSyntaxException e) {
        lastError = e;
        // RegExParameter.logger.warn("Error in RegEx: " + regex, e);
      }
    }
    return null;
  }

  public Throwable getLastError() {
    return lastError;
  }

  @Override
  public void formString(final String strValue) {
    if (LibStr.isEmptyOrNull(strValue)) {
      value = defValue;
    }
    else {
      value = decodePattern(strValue);
    }
  }

}
