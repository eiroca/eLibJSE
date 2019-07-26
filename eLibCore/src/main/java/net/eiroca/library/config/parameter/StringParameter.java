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

public class StringParameter extends Parameter<String> {

  private static final String QUOTE = "\"";
  boolean trimQuote = true;

  public StringParameter(final Parameters owner, final String paramName, final String paramDef, final boolean required, final boolean nullable) {
    super(owner, paramName, paramDef, required, nullable);
  }

  public StringParameter(final Parameters owner, final String paramName, final String paramDef) {
    super(owner, paramName, paramDef);
  }

  public StringParameter(final Parameters owner, final String paramName, final String paramDef, final boolean trimQuote) {
    this(owner, paramName, paramDef);
    this.trimQuote = trimQuote;
  }

  public StringParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  private String decodeStr(String trimStr) {
    if (trimStr == null) { return null; }
    trimStr = trimStr.trim();
    if (trimStr.length() >= 2) {
      if ((trimStr.startsWith(StringParameter.QUOTE)) && (trimStr.endsWith(StringParameter.QUOTE))) {
        trimStr = trimStr.substring(StringParameter.QUOTE.length(), trimStr.length() - StringParameter.QUOTE.length());
      }
    }
    return trimStr;
  }

  @Override
  public String convertString(final String strValue) {
    String value;
    if (LibStr.isEmptyOrNull(strValue)) {
      value = getDefault();
    }
    else {
      if (trimQuote) {
        value = decodeStr(strValue);
      }
      else {
        value = strValue;
      }
    }
    return value;
  }

  public boolean isTrimQuote() {
    return trimQuote;
  }

  public void setTrimQuote(final boolean trimQuote) {
    this.trimQuote = trimQuote;
  }

  @Override
  public boolean isValid(final Object value) {
    return value instanceof String;
  }

}
