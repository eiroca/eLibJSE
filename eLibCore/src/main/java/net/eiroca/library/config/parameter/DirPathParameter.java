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

import java.nio.file.Path;
import net.eiroca.library.config.Parameter;
import net.eiroca.library.config.Parameters;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;

public class DirPathParameter extends Parameter<String> {

  public DirPathParameter(final Parameters owner, final String paramName, final String defPathStr, final boolean required, final boolean nullable) {
    super(owner, paramName, null, required, nullable);
    String value = Helper.getDirPath(defPathStr).toString();
    if (!value.endsWith(Helper.FS)) {
      value = value + Helper.FS;
    }
    defValue = value;
  }

  public DirPathParameter(final Parameters owner, final String paramName, final String defPathStr) {
    this(owner, paramName, defPathStr, false, false);
  }

  public DirPathParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  @Override
  public String convertString(final String strValue) {
    String value;
    if (LibStr.isEmptyOrNull(strValue)) {
      value = getDefault();
    }
    else {
      final Path path = Helper.getDirPath(strValue);
      value = path.toString();
      if (value == null) {
        value = getDefault();
      }
      else {
        if (!value.endsWith(Helper.FS)) {
          value = value + Helper.FS;
        }
      }
    }
    return value;
  }

  @Override
  public boolean isValid(final Object value) {
    return value instanceof Path;
  }

}
