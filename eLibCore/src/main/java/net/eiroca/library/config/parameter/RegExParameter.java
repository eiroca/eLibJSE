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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.eiroca.library.config.Parameter;
import net.eiroca.library.config.Parameters;
import net.eiroca.library.core.LibStr;

public class RegExParameter extends Parameter<Pattern> {

  private Throwable lastError = null;

  public RegExParameter(final Parameters owner, final String paramName, final String defRegExStr, final boolean required, final boolean nullable) {
    super(owner, paramName, RegExParameter._decodePattern(defRegExStr), required, nullable);
  }

  public RegExParameter(final Parameters owner, final String paramName, final String defRegExStr) {
    super(owner, paramName, RegExParameter._decodePattern(defRegExStr));
  }

  public RegExParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  private static Pattern _decodePattern(final String regex) throws PatternSyntaxException {
    if (regex != null) { return Pattern.compile(regex); }
    return null;
  }

  protected Pattern decodePattern(final String regex) {
    lastError = null;
    try {
      return RegExParameter._decodePattern(regex);
    }
    catch (final PatternSyntaxException e) {
      lastError = e;
    }
    return null;
  }

  public Throwable getLastError() {
    return lastError;
  }

  @Override
  public Pattern convertString(final String strValue) {
    Pattern value;
    if (LibStr.isEmptyOrNull(strValue)) {
      value = getDefault();
    }
    else {
      value = decodePattern(strValue);
    }
    return value;
  }

  @Override
  public String encodeString(final Object val) {
    return ((Pattern)val).pattern();
  }

  @Override
  public boolean isValid(final Object value) {
    return value instanceof Pattern;
  }

}
