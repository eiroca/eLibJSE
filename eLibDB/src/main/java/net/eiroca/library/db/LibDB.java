/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import net.eiroca.library.core.Helper;
import net.eiroca.library.system.Logs;

public class LibDB {

  private final static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  private final static Logger logger = Logs.getLogger();

  public static void insertRecord(final Connection conn, final String table, final String[] fields, final Object[] values, final int limit) throws SQLException {
    Statement statement = null;
    final StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ").append(table);
    sb.append(" (");
    for (int i = 0; i < fields.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(fields[i]);
    }
    sb.append(')');
    sb.append(" VALUES ");
    sb.append('(');
    for (int i = 0; i < values.length; i++) {
      final Object o = values[i];
      if (i > 0) {
        sb.append(',');
      }
      if (o == null) {
        sb.append("NULL");
      }
      else if (o instanceof Date) {
        sb.append("to_date('" + LibDB.dateFormat.format((Date)o) + "', 'yyyy/mm/dd hh24:mi:ss')");
      }
      else if (o instanceof Double) {
        sb.append(o.toString());
      }
      else if (o instanceof String) {
        String datum = String.valueOf(o);
        if ((limit > 0) && (datum.length() > limit)) {
          datum = datum.substring(0, limit);
        }
        datum = datum.replaceAll("'", "''");
        sb.append("'" + datum + "'");
      }
      else {
        sb.append(String.valueOf(o));
      }
    }
    sb.append(')');
    try {
      LibDB.logger.trace(sb.toString());
      // execute insert SQL stetement
      statement = conn.createStatement();
      statement.executeUpdate(sb.toString());
    }
    catch (final SQLException e) {
      LibDB.logger.warn(sb.toString());
      throw e;
    }
    finally {
      Helper.close(statement);
    }
  }

  public static void callSP(Connection conn, String spName, Object[] params, int timeout) throws SQLException {
    Statement statement = null;
    final StringBuilder sb = new StringBuilder();
    sb.append("CALL ").append(spName);
    sb.append("(");
    boolean first = true;
    for (Object p : params) {
      if (!first) sb.append(",");
      sb.append("'");
      sb.append(p != null ? String.valueOf(p) : "");
      sb.append("'");
      first = false;
    }
    sb.append(")");
    try {
      LibDB.logger.trace(sb.toString());
      // execute insert SQL stetement
      statement = conn.createStatement();
      statement.executeUpdate(sb.toString());
      if (timeout > 0) statement.setQueryTimeout(timeout);
    }
    catch (final SQLException e) {
      LibDB.logger.warn(sb.toString());
      throw e;
    }
    finally {
      Helper.close(statement);
    }
  }

}
