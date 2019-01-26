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
package net.eiroca.library.diagnostics.monitors;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.db.DBConfig;
import net.eiroca.library.db.DBQuery;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.SQLchecks;
import net.eiroca.library.diagnostics.validators.GenericValidator;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.metrics.MeasureSplitting;
import net.eiroca.library.system.IContext;
import net.eiroca.library.system.ILog.LogLevel;
import net.eiroca.library.system.LibSystem;

public class DatabaseMonitor extends TCPServerMonitor {

  public enum CaputeMode {
    SINGLE, COLUMNS, ROW_SINGLEMETRIC, ROW_MULTIMETRIC, MIXED
  }

  private static final String CONFIG_CAPTUREMODE = "CaptureMode";
  private static final HashMap<String, CaputeMode> CONFIG_CAPTUREMODE_VAL = new HashMap<>();
  static {
    DatabaseMonitor.CONFIG_CAPTUREMODE_VAL.put("Single value", CaputeMode.SINGLE);
    DatabaseMonitor.CONFIG_CAPTUREMODE_VAL.put("Metrics on columns", CaputeMode.COLUMNS);
    DatabaseMonitor.CONFIG_CAPTUREMODE_VAL.put("Metrics on rows", CaputeMode.ROW_SINGLEMETRIC);
    DatabaseMonitor.CONFIG_CAPTUREMODE_VAL.put("Rows with multiple metrics", CaputeMode.ROW_MULTIMETRIC);
    DatabaseMonitor.CONFIG_CAPTUREMODE_VAL.put("Mixed mode", CaputeMode.MIXED);
  }

  private static final String CONFIG_RESULTCOLUMN = "ResultColumn";
  private static final String CONFIG_VALIDATECOLUMN = "ValidateColumn";

  private static final String CONFIG_SPLITTINGNAME = "SplittingName";
  private static final String CONFIG_SPLITTINGNAME_DEFAULT = "metrics";

  private static final String CONFIG_RUNSQL = "runSQL";

  MeasureGroup mgDBMonitor = new MeasureGroup("Database Monitor", "Database - {0}");
  Measure mDBQueryTime = new Measure(mgDBMonitor, "Query Time");
  Measure mDBQueryRows = new Measure(mgDBMonitor, "Query Rows");

  protected GenericValidator validator;
  protected DBConfig config;
  protected DBQuery dbSQL;

  protected SQLchecks[] checks = null;
  protected String dbVariant = null;
  protected boolean runSQL = true;
  protected Object[] sqlParams = null;

  protected String metricGroup;
  protected String resultColumn;
  protected CaputeMode captureMode;
  protected String validateColumn;

  protected double queryResult = 0.0;
  protected int rowcount = 0;
  protected boolean succed = false;

  protected long startTime = 0;
  protected long endTime = 0;
  protected long connectStartTime = 0;
  protected long connectEndTime = 0;
  protected long queryStartTime = 0;
  protected long queryEndTime = 0;
  protected Connection con = null;

  @Override
  public void setup(final IContext context) throws CommandException {
    validator = new GenericValidator();
    config = new DBConfig(context);
    dbSQL = new DBQuery(context);
    dbVariant = null;
    super.setup(context);
  }

  @Override
  public void readConf() throws CommandException {
    validator.setup(context);
    config.setup(context);
    dbSQL.setup(context);
    metricGroup = context.getConfigString(DatabaseMonitor.CONFIG_SPLITTINGNAME, DatabaseMonitor.CONFIG_SPLITTINGNAME_DEFAULT);
    resultColumn = context.getConfigString(DatabaseMonitor.CONFIG_RESULTCOLUMN, null);
    final String modeStr = context.getConfigString(DatabaseMonitor.CONFIG_CAPTUREMODE, null);
    captureMode = DatabaseMonitor.CONFIG_CAPTUREMODE_VAL.get(modeStr);
    if (captureMode == null) {
      CommandException.ConfigurationError("Invalid capture mode:" + modeStr);
    }
    validateColumn = context.getConfigString(DatabaseMonitor.CONFIG_VALIDATECOLUMN, null);
    runSQL = context.getConfigBoolean(DatabaseMonitor.CONFIG_RUNSQL, true);
  }

  @Override
  public void close() throws Exception {
    Helper.close(validator);
    validator = null;
    config = null;
    super.close();
  }

  @Override
  public void loadMetricGroup(final List<MeasureGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgDBMonitor);
    if (checks != null) {
      for (final SQLchecks check : checks) {
        groups.add(check.mg);
      }
    }
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    super.preCheck(host);
    config.setServer(targetHost.getHostString());
    queryResult = 0.0;
    rowcount = 0;
    succed = false;
    startTime = 0;
    endTime = 0;
    connectStartTime = 0;
    connectEndTime = 0;
    queryStartTime = 0;
    queryEndTime = 0;

    startTime = System.nanoTime();
    // connect to the database
    if (!config.prepareConnection()) {
      CommandException.ConfigurationError("Unable to create DB connection URL. " + config.getLastError().getMessage());
    }
    connectStartTime = System.nanoTime();
    con = config.getConnection();
    final Exception lastError = config.getLastError();
    if (con == null) {
      CommandException.InfrastructureError("Unable to connect to " + config.getConnectionUrl() + ". " + lastError.getMessage());
    }
    connectEndTime = System.nanoTime();
    // Update server metrics
    mServerReachable.setValue(1.0);
    mServerLatency.setValue(net.eiroca.library.core.Helper.elapsed(connectStartTime, connectEndTime));
    mServerSocketTimeout.setValue(0.0);
    mServerConnectionTimeout.setValue(lastError == null ? 0.0 : (lastError instanceof SQLTimeoutException ? 1.0 : 0.0));
    return true;
  }

  @Override
  public boolean runCheck() throws CommandException {
    if (runSQL) {
      runSQL();
    }
    if (checks != null) {
      executeSQLchecks(checks, dbVariant);
    }
    return true;
  }

  @Override
  public boolean postCheck() throws CommandException {
    endTime = System.nanoTime();
    mServerResponseTime.setValue(net.eiroca.library.core.Helper.elapsed(startTime, endTime));
    mServerResult.setValue(queryResult);
    mServerStatus.setValue(!succed);
    mDBQueryRows.setValue(rowcount);
    return true;
  }

  public void runSQL() throws CommandException {
    // Create and execute an SQL statement
    succed = dbSQL.prepareStatement(con);
    if (!succed) {
      context.info("Error: ", dbSQL.getErrorDesc());
      CommandException.ConfigurationError("Unable to create DB statement");
    }
    context.debug("Executing SQL: ", dbSQL.SQL);
    queryStartTime = System.nanoTime();
    boolean ok = dbSQL.execute();
    queryEndTime = System.nanoTime();
    mDBQueryTime.setValue(net.eiroca.library.core.Helper.elapsed(queryStartTime, queryEndTime));
    if (!ok) {
      CommandException.InfrastructureError("Error -> " + dbSQL.getErrorDesc());
    }
    // Reading data
    rowcount = 0;
    queryResult = 0.0;
    final String[] data = new String[dbSQL.recordSize()];
    while (true) {
      ok = dbSQL.next();
      if (dbSQL.isEOF()) {
        break;
      }
      if (!ok) {
        CommandException.InfrastructureError("SQL Error. " + dbSQL.getErrorDesc());
      }
      rowcount++;
      context.trace("Processing row: ", rowcount);
      if (rowcount == 1) {
        final boolean validResult = checkColumn(validateColumn);
        mServerVerified.setValue(validResult ? 1.0 : 0.0);
        context.info("Validating column: ", validateColumn, " -> ", validResult);
      }
      if (captureMode == CaputeMode.SINGLE) {
        if (resultColumn != null) {
          queryResult = dbSQL.getDouble(resultColumn, 0.0);
        }
        else {
          queryResult = dbSQL.getDouble(1, 0.0);
        }
        break;
      }
      ok = dbSQL.fetchRecord(data);
      if (ok) {
        if (captureMode == CaputeMode.COLUMNS) {
          final MeasureSplitting ms = mServerResult.getSplitting(metricGroup);
          for (int i = 0; i < data.length; i++) {
            final String splitName = dbSQL.getColumnsName(i);
            final Double val = net.eiroca.library.core.Helper.getDouble(data[i], 0.0);
            ms.setValue(splitName, val);
            queryResult += val;
            context.logF(LogLevel.debug, "{0}[{1}]={2}", ms.getName(), splitName, val);
          }
          break;
        }
        else if ((captureMode == CaputeMode.ROW_SINGLEMETRIC) || (captureMode == CaputeMode.ROW_MULTIMETRIC)) {
          try {
            int base = 0;
            int cnt = data.length / 3;
            if (captureMode == CaputeMode.ROW_SINGLEMETRIC) {
              cnt = 1;
            }
            for (int i = 1; i <= cnt; i++) {
              final String splitGroup = data[base++];
              final String splitName = data[base++];
              final String valStr = data[base++];
              if (valStr == null) {
                continue;
              }
              final Double val = net.eiroca.library.core.Helper.getDouble(valStr, 0.0);
              context.logF(LogLevel.debug, "{0}[{1}]={2}", splitGroup, splitName, val);
              final MeasureSplitting ms = mServerResult.getSplitting(splitGroup);
              ms.setValue(splitName, val);
              queryResult += val;
            }
          }
          catch (final Exception e) {
          }
        }
        else if (captureMode == CaputeMode.MIXED) {
          final String splitGroup = data[0];
          for (int i = 1; i <= data.length; i++) {
            final String splitName = dbSQL.getColumnsName(i);
            final String valStr = data[i];
            if (valStr == null) {
              continue;
            }
            final Double val = net.eiroca.library.core.Helper.getDouble(valStr, 0.0);
            context.logF(LogLevel.debug, "{0}[{1}]={2}", splitGroup, splitName, val);
            final MeasureSplitting ms = mServerResult.getSplitting(splitGroup);
            ms.setValue(splitName, val);
            queryResult += val;
          }
        }
      }
      else {
        succed = false;
        break;
      }
    }
    context.info("Parsed ", rowcount, " row(s).");
    dbSQL.close();
  }

  /**
   * compareColumnValue Compares the contents of the columnName column against the value in the
   * columnValue variable. Sets the contentMatched flag to true or false, depending on on the
   * result.
   *
   * Returns null if everything worked OK Status with error code if the data entered by the user was
   * determined to be invalid.
   */
  protected boolean checkColumn(final String matchColumn) throws CommandException {
    String columnValueStr = null;
    if (LibStr.isEmptyOrNull(matchColumn)) {
      columnValueStr = dbSQL.getString(1, null);
    }
    else {
      columnValueStr = dbSQL.getString(matchColumn, null);
    }
    if (columnValueStr == null) {
      final String errorDesc = "Error retrieving column " + (LibStr.isEmptyOrNull(matchColumn) ? "[1]" : matchColumn) + " from the resultset. ";
      context.error(errorDesc);
      CommandException.InfrastructureError(errorDesc);
    }
    return validator.isValid(columnValueStr);
  }

  protected void executeSQLchecks(final SQLchecks[] checks, final String variant) throws CommandException {
    try {
      if (con == null) {
        CommandException.InfrastructureError("Unable to connect to " + config.getConnectionUrl() + ". " + config.getLastError().getMessage());
      }
      for (final SQLchecks check : checks) {
        final String SQL = check.getSQL(variant);
        if (SQL == null) {
          CommandException.ConfigurationError("Unable to load SQL for " + check.SQLscript);
        }
        switch (check.type) {
          case 1:
            populateSimpleMetrics(check.mg, con, SQL, sqlParams);
            break;
          case 2:
            populateSplittedMetrics(check.mg, check.param, con, SQL, sqlParams);
            break;
          default:
            CommandException.ConfigurationError("Invalid check type for " + check.mg.getName());
        }
      }
    }
    catch (final Exception e) {
      CommandException.InfrastructureError(net.eiroca.library.core.Helper.getExceptionAsString(e, false));
    }
    finally {
      net.eiroca.library.core.Helper.close(con);
    }
  }

  public void populateSimpleMetrics(final MeasureGroup mg, final Connection con, String query, final Object[] params) throws Exception {
    ResultSet rs = null;
    PreparedStatement st = null;
    if ((params != null) && (query.contains("{"))) {
      context.debug("Format String: ", query);
      query = MessageFormat.format(query, params);
    }
    try {
      context.debug("Excecuting: ", query);
      st = con.prepareStatement(query);
      rs = st.executeQuery();
      int rows = 0;
      while (rs.next()) {
        final String metric = rs.getString(2);
        final double metricValue = rs.getDouble(3);
        context.trace("Processing: ", metric);
        rows++;
        mg.setValue(metric, metricValue);
      }
      context.debug("Processed rows:", rows);
    }
    catch (final SQLException e) {
      LibSystem.trace(context, e, false);
    }
    finally {
      net.eiroca.library.core.Helper.close(rs, st);
    }
  }

  public void populateSplittedMetrics(final MeasureGroup mg, final String splitName, final Connection con, String query, final Object[] params) throws Exception {
    ResultSet rs = null;
    PreparedStatement st = null;
    if ((params != null) && (query.contains("{"))) {
      context.debug("Format String: ", query);
      query = MessageFormat.format(query, params);
    }
    try {
      context.debug("Executing: ", query);
      st = con.prepareStatement(query);
      rs = st.executeQuery();
      final ResultSetMetaData rsmd = rs.getMetaData();
      int rows = 0;
      while (rs.next()) {
        final String splitting = rs.getString(1);
        context.trace("Processing: ", splitting);
        rows++;
        for (int i = 2; i <= rsmd.getColumnCount(); i++) {
          final String metric = rsmd.getColumnName(i);
          final double metricValue = rs.getDouble(i);
          mg.setValue(metric, splitName, splitting, metricValue);
        }
      }
      context.debug("Processed rows:", rows);
    }
    catch (final SQLException e) {
      context.info("errore!");
      LibSystem.trace(context, e, false);
    }
    finally {
      net.eiroca.library.core.Helper.close(rs, st);
    }
  }

}
