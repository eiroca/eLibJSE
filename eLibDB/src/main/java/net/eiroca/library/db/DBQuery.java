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
package net.eiroca.library.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.IConfig;
import net.eiroca.library.system.IContext;
import net.eiroca.library.system.ILog;

public class DBQuery {

  private static final String CONFIG_TIMEOUT = "SQLTimeout";
  private static final String CONFIG_STATEMENT = "SQLQuery";
  private static final String CONFIG_COLUMNS = "SQLColumns";

  public String SQL;
  public int timeout;
  public String columns;

  protected Statement stmt = null;
  protected ResultSet rs = null;

  protected String[] readColName = null;
  protected int[] readColNum = null;
  protected String[] columnNames = null;

  protected ILog log;
  protected String errorDesc = null;

  public String getErrorDesc() {
    return errorDesc;
  }

  protected boolean EOF = false;

  public DBQuery(final IContext context) {
    log = context;
    setup(context);
  }

  public void setup(final IConfig config) {
    setSQL(config.getConfigString(DBQuery.CONFIG_STATEMENT, null));
    setTimeout(config.getConfigInt(DBQuery.CONFIG_TIMEOUT, 0));
    setColumns(config.getConfigString(DBQuery.CONFIG_COLUMNS, null));
  }

  public String getSQL() {
    return SQL;
  }

  public void setSQL(final String sQL) {
    SQL = sQL;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(final int timeout) {
    this.timeout = timeout;
  }

  public String getColumns() {
    return columns;
  }

  public void setColumns(final String columns) {
    this.columns = columns;
    if (columns != null) {
      readColName = Helper.split(columns, true);
    }
    else {
      readColName = null;
    }
    readColNum = null;
  }

  public boolean prepareStatement(final Connection con) {
    boolean succed = con != null;
    if (succed) {
      readColNum = null;
      try {
        stmt = con.createStatement();
        if (timeout > 0) {
          stmt.setQueryTimeout(timeout);
        }
      }
      catch (final SQLException e) {
        succed = false;
        errorDesc = Helper.getExceptionAsString(e, true);
        log.error(errorDesc);
      }
    }
    else {
      errorDesc = "Wrong or missing connection";
    }
    return succed;
  }

  public String getFieldName(final int idx) {
    String result = null;
    try {
      final ResultSetMetaData metadata = rs.getMetaData();
      result = metadata.getColumnName(idx + 1);
    }
    catch (final SQLException e) {
      errorDesc = "Error: " + e.getMessage();
      log.error(errorDesc);
    }
    return result;
  }

  public boolean execute() {
    boolean succed = true;
    EOF = true;
    try {
      rs = stmt.executeQuery(SQL);
      final ResultSetMetaData metadata = rs.getMetaData();
      if (readColName != null) {
        // Resolve column names -> column index
        readColNum = new int[readColName.length];
        for (int i = 0; i < readColNum.length; i++) {
          readColNum[i] = -1;
          for (final int j = 1; j <= metadata.getColumnCount(); i++) {
            if (readColName[i].equalsIgnoreCase(metadata.getColumnTypeName(j))) {
              readColNum[i] = j + 1;
              break;
            }
          }
        }
      }
      else {
        final int size = metadata.getColumnCount();
        readColNum = new int[size];
        for (int i = 0; i < size; i++) {
          readColNum[i] = i + 1;
        }
      }
      columnNames = new String[readColNum.length];
      for (int i = 0; i < readColNum.length; i++) {
        final int idx = readColNum[i];
        final String colName = (idx > 0) ? metadata.getColumnName(idx) : null;
        columnNames[i] = colName != null ? colName : "[" + (idx) + "]";
      }
      EOF = false;
    }
    catch (final Exception e) {
      Helper.close(rs, stmt);
      succed = false;
      errorDesc = MessageFormat.format("{0}: {1}", e.getClass().getName(), e.getMessage());
      log.error(errorDesc);
    }
    return succed;
  }

  public int recordSize() {
    return readColNum != null ? readColNum.length : 0;
  }

  public void close() {
    Helper.close(rs, stmt);
    rs = null;
    stmt = null;
    EOF = true;
  }

  public boolean isEOF() {
    return EOF;
  }

  public boolean next() {
    boolean result = true;
    try {
      EOF = !rs.next();
    }
    catch (final SQLException e) {
      errorDesc = "Error: " + e.getMessage();
      log.error(errorDesc);
      result = false;
    }
    return result;
  }

  public boolean fetchRecord(final String[] data) {
    boolean succed = true;
    try {
      for (int i = 0; i < readColNum.length; i++) {
        final int idx = readColNum[i];
        if (idx > 0) {
          data[i] = rs.getString(idx);
        }
        else {
          data[i] = null;
        }
      }
    }
    catch (final SQLException e) {
      succed = false;
      errorDesc = "Error: " + e.getMessage();
      log.error(errorDesc);
    }
    return succed;
  }

  public double getDouble(final String columnName, final double defVal) {
    return Helper.getDouble(getString(columnName, null), defVal);
  }

  public double getDouble(final int colNum, final double defVal) {
    return Helper.getDouble(getString(colNum, null), defVal);
  }

  public String getString(final String columnName, final String defVal) {
    if (LibStr.isEmptyOrNull(columnName)) { return defVal; }
    int colNum = -1;
    try {
      colNum = rs.findColumn(columnName);
    }
    catch (final SQLException e1) {
      colNum = Helper.getInt(columnName, -1);
    }
    if (colNum < 0) {
      errorDesc = "Invalid column: " + columnName;
      log.error(errorDesc);
      return defVal;
    }
    String colVal = null;
    try {
      colVal = rs.getString(colNum);
    }
    catch (final SQLException e) {
      errorDesc = columnName + " -> error: " + e.getMessage();
      log.error(errorDesc);
    }
    return colVal == null ? defVal : colVal;
  }

  public String getString(final int colNum, final String defVal) {
    String colVal = null;
    try {
      colVal = rs.getString(colNum);
    }
    catch (final SQLException e) {
      int n = -1;
      try {
        n = rs.getMetaData().getColumnCount();
      }
      catch (final SQLException e1) {
      }
      errorDesc = colNum + " / " + n + " -> error: " + e.getMessage();
      log.error(errorDesc);
    }
    return colVal == null ? defVal : colVal;
  }

  public String[] getColumnsName() {
    return columnNames;
  }

  public String getColumnsName(final int idx) {
    return columnNames[idx];
  }

  public static double getResultSetDoubleValue(final ResultSet rs) throws SQLException {
    double d = Double.NaN;
    if (rs == null) { return d; }
    while (rs.next()) {
      d = rs.getDouble(1);
    }
    return d;
  }

}
