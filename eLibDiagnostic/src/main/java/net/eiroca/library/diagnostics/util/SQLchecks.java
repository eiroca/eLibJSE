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
package net.eiroca.library.diagnostics.util;

import java.text.MessageFormat;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.system.LibFile;

public class SQLchecks {

  public String SQLscript;
  public MeasureGroup mg;
  public int type;
  public String param;

  public SQLchecks(final String measureGroupName, final String format, final String SQLscript) {
    this(measureGroupName, format, SQLscript, 1, null);
  }

  public SQLchecks(final String measureGroupName, final String format, final String SQLscript, final int type, final String param) {
    mg = new MeasureGroup(measureGroupName, format);
    this.type = type;
    this.param = param;
    this.SQLscript = SQLscript;
  }

  public String getSQL(final String variant) {
    String SQL = null;
    if (variant != null) {
      SQL = LibFile.readFile(MessageFormat.format(SQLscript, variant));
    }
    if (SQL == null) {
      SQL = LibFile.readFile(SQLscript);
    }
    if (SQL != null) {
      SQL = SQL.replaceAll("([\r\n\t]+)", " ").trim();
    }
    return SQL;
  }
}
