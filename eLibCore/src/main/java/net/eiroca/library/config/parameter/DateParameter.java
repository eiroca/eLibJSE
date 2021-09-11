/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.eiroca.library.config.Parameter;
import net.eiroca.library.config.Parameters;
import net.eiroca.library.core.LibDate;
import net.eiroca.library.core.LibStr;

public class DateParameter extends Parameter<Date> {

  public List<SimpleDateFormat> formats = new ArrayList<>();

  public DateParameter(final Parameters owner, final String paramName, final Date defValue, final String... dateFormat) {
    super(owner, paramName, defValue, true, false);
    for (final String f : dateFormat) {
      formats.add(new SimpleDateFormat(f));
    }
  }

  public DateParameter(final Parameters owner, final String paramName, final Date defValue, final SimpleDateFormat... dateFormat) {
    super(owner, paramName, defValue, true, false);
    for (final SimpleDateFormat f : dateFormat) {
      formats.add(f);
    }
  }

  public DateParameter(final Parameters owner, final String paramName, final Date defValue, final boolean required, final boolean nullable) {
    super(owner, paramName, defValue, required, nullable);
    formats.add(LibDate.SDF);
  }

  public DateParameter(final Parameters owner, final String paramName, final boolean required) {
    super(owner, paramName, null, required, true);
    formats.add(LibDate.SDF);
  }

  public DateParameter(final Parameters owner, final String paramName, final Date defValue) {
    super(owner, paramName, defValue);
    formats.add(LibDate.SDF);
  }

  public DateParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
    formats.add(LibDate.SDF);
  }

  @Override
  public Date convertString(final String strValue) {
    Date value = getDefault();
    if (LibStr.isEmptyOrNull(strValue)) { return value; }
    for (final SimpleDateFormat df : formats) {
      try {
        value = df.parse(strValue);
        break;
      }
      catch (final ParseException e) {
      }
    }
    return value;
  }

  @Override
  public boolean isValid(final Object value) {
    return value instanceof Integer;
  }

}
