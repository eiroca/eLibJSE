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

import java.util.List;
import net.eiroca.library.db.DBConfig;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.SQLchecks;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.IContext;

public class OracleMonitor extends DatabaseMonitor {

  private static final String CONFIG_ORACLEVERSION = "OracleVersion";

  protected final MetricGroup mgOracle = new MetricGroup("Oracle Statistics", "Oracle - {0}");
  protected final Measure mInfoSystemdate = mgOracle.createMeasure("Info - System date", "Info - System date", "number");
  protected final Measure mSGAFreeBufferWaits = mgOracle.createMeasure("SGA - Free Buffer Waits", "SGA - Free Buffer Waits", "number");
  protected final Measure mSGAWriteCompleteWaits = mgOracle.createMeasure("SGA - Write Complete Waits", "SGA - Write Complete Waits", "number");
  protected final Measure mSGABufferBusyWaits = mgOracle.createMeasure("SGA - Buffer Busy Waits", "SGA - Buffer Busy Waits", "number");
  protected final Measure mSGADBBlockChanges = mgOracle.createMeasure("SGA - DB Block Changes", "SGA - DB Block Changes", "number");
  protected final Measure mSGADBBlockGets = mgOracle.createMeasure("SGA - DB Block Gets", "SGA - DB Block Gets", "number");
  protected final Measure mSGAConsistentGets = mgOracle.createMeasure("SGA - Consistent Gets", "SGA - Consistent Gets", "number");
  protected final Measure mSGAPhysicalReads = mgOracle.createMeasure("SGA - Physical Reads", "SGA - Physical Reads", "number");
  protected final Measure mSGAPhysicalWrites = mgOracle.createMeasure("SGA - Physical Writes", "SGA - Physical Writes", "number");
  protected final Measure mSGABufferCacheHitRatio = mgOracle.createMeasure("SGA - Buffer Cache Hit Ratio", "SGA - Buffer Cache Hit Ratio", "percent");
  protected final Measure mSGAExecutionWithoutParseRatio = mgOracle.createMeasure("SGA - Execution Without Parse Ratio", "SGA - Execution Without Parse Ratio", "percent");
  protected final Measure mSGAMemorySortRatio = mgOracle.createMeasure("SGA - Memory Sort Ratio", "SGA - Memory Sort Ratio", "percent");
  protected final Measure mSessionsMaximumConcurrentUser = mgOracle.createMeasure("Sessions - Maximum Concurrent User", "Sessions - Maximum Concurrent User", "number");
  protected final Measure mSessionsCurrentConcurrentUser = mgOracle.createMeasure("Sessions - Current Concurrent User", "Sessions - Current Concurrent User", "number");
  protected final Measure mSessionsHighestConcurrentUser = mgOracle.createMeasure("Sessions - Highest Concurrent User", "Sessions - Highest Concurrent User", "number");
  protected final Measure mSessionsMaximumNamedUsers = mgOracle.createMeasure("Sessions - Maximum Named Users", "Sessions - Maximum Named Users", "number");
  protected final Measure mHitRatioSQLAreaGet = mgOracle.createMeasure("Hit Ratio - SQL Area Get", "Hit Ratio - SQL Area Get", "percent");
  protected final Measure mHitRatioSQLAreaPin = mgOracle.createMeasure("Hit Ratio - SQL Area Pin", "Hit Ratio - SQL Area Pin", "percent");
  protected final Measure mHitRatioTableProcedureGet = mgOracle.createMeasure("Hit Ratio - Table/Procedure Get", "Hit Ratio - Table/Procedure Get", "percent");
  protected final Measure mHitRatioTableProcedurePin = mgOracle.createMeasure("Hit Ratio - Table/Procedure Pin", "Hit Ratio - Table/Procedure Pin", "percent");
  protected final Measure mHitRatioBodyGet = mgOracle.createMeasure("Hit Ratio - Body Get", "Hit Ratio - Body Get", "percent");
  protected final Measure mHitRatioBodyPin = mgOracle.createMeasure("Hit Ratio - Body Pin", "Hit Ratio - Body Pin", "percent");
  protected final Measure mHitRatioTriggerGet = mgOracle.createMeasure("Hit Ratio - Trigger Get", "Hit Ratio - Trigger Get", "percent");
  protected final Measure mHitRatioTriggerPin = mgOracle.createMeasure("Hit Ratio - Trigger Pin", "Hit Ratio - Trigger Pin", "percent");
  protected final Measure mHitRatioLibraryCacheGet = mgOracle.createMeasure("Hit Ratio - Library Cache Get", "Hit Ratio - Library Cache Get", "percent");
  protected final Measure mHitRatioLibraryCachePin = mgOracle.createMeasure("Hit Ratio - Library Cache Pin", "Hit Ratio - Library Cache Pin", "percent");
  protected final Measure mHitRatioDictionaryCache = mgOracle.createMeasure("Hit Ratio - Dictionary Cache", "Hit Ratio - Dictionary Cache", "percent");
  protected final Measure mSharedPoolFreeMemory = mgOracle.createMeasure("Shared Pool - Free Memory", "Shared Pool - Free Memory", "number");
  protected final Measure mSharedPoolReloads = mgOracle.createMeasure("Shared Pool - Reloads", "Shared Pool - Reloads", "number");
  protected final Measure mLatchesWaitLatchGets = mgOracle.createMeasure("Latches - Wait Latch Gets", "Latches - Wait Latch Gets", "number");
  protected final Measure mLatchesImmediateLatchGets = mgOracle.createMeasure("Latches - Immediate Latch Gets", "Latches - Immediate Latch Gets", "number");
  protected final Measure mRedoSpaceWaitRatio = mgOracle.createMeasure("Redo - Space Wait Ratio", "Redo - Space Wait Ratio", "percent");
  protected final Measure mRedoAllocationLatch = mgOracle.createMeasure("Redo - Allocation Latch", "Redo - Allocation Latch", "number");
  protected final Measure mRedoCopyLatches = mgOracle.createMeasure("Redo - Copy Latches", "Redo - Copy Latches", "number");
  protected final Measure mInfoRecursiveCallsRatio = mgOracle.createMeasure("Info - Recursive Calls Ratio", "Info - Recursive Calls Ratio", "percent");
  protected final Measure mInfoShortTableScansRatio = mgOracle.createMeasure("Info - Short Table Scans Ratio", "Info - Short Table Scans Ratio", "percent");
  protected final Measure mInfoRollbackSegmentContention = mgOracle.createMeasure("Info - Rollback Segment Contention", "Info - Rollback Segment Contention", "number");
  protected final Measure mInfoCPUParseOverhead = mgOracle.createMeasure("Info - CPU Parse Overhead", "Info - CPU Parse Overhead", "number");
  protected final Measure mTableContentionChainedFetchRatio = mgOracle.createMeasure("Table Contention - Chained Fetch Ratio", "Table Contention - Chained Fetch Ratio", "percent");
  protected final Measure mTableContentionFreeListContention = mgOracle.createMeasure("Table Contention - Free List Contention", "Table Contention - Free List Contention", "number");

  protected final MetricGroup mgOracleTablespace = new MetricGroup("Oracle Statistics", "Oracle - Tablespace - {0}");
  protected final Measure mTablespaceTotal = mgOracleTablespace.createMeasure("Total", "Tablespace - Total Space (MB)", "megabytes");
  protected final Measure mTablespaceUsed = mgOracleTablespace.createMeasure("Used", "Tablespace - Used Space (MB)", "megabytes");
  protected final Measure mTablespaceFree = mgOracleTablespace.createMeasure("Free", "Tablespace - Free Space (MB)", "megabytes");
  protected final Measure mTablespaceUsedPercent = mgOracleTablespace.createMeasure("Used %", "Tablespace - Used Space (%)", "percent");
  protected final Measure mTablespaceFreePercent = mgOracleTablespace.createMeasure("Free %", "Tablespace - Total Space (%)", "percent");

  protected final MetricGroup mgOracleWaiter = new MetricGroup("Oracle Statistics", "Oracle - Waiter - {0}");
  protected final Measure mWaiterTotalWaits = mgOracleWaiter.createMeasure("Total Waits", "Waiter - Total Waits", "number");
  protected final Measure mWaiterTotalTimeouts = mgOracleWaiter.createMeasure("Total Timeouts", "Waiter - Total Timeouts", "number");
  protected final Measure mWaiterTimeWaited = mgOracleWaiter.createMeasure("Time Waited", "Waiter - Time Waited", "s");
  protected final Measure mWaiterAverageWaittime = mgOracleWaiter.createMeasure("Average Wait time", "Waiter - Average Wait time", "s");

  protected final MetricGroup mgOracleLock = new MetricGroup("Oracle Statistics", "Oracle - Lock - {0}");
  protected final Measure mLockLockMode = mgOracleLock.createMeasure("Lock Mode", "Lock - Lock Mode", "number");
  protected final Measure mLockLockStatus = mgOracleLock.createMeasure("Lock Status", "Lock - Lock Status", "number");
  protected final Measure mLockLocks = mgOracleLock.createMeasure("Locks", "Lock count", "number");

  protected final MetricGroup mgOracleTopSQL = new MetricGroup("Oracle Statistics", "Oracle - TopSQL - {0}");
  protected final Measure mTopSQLElapsedTime = mgOracleTopSQL.createMeasure("Elapsed Time", "Elapsed Time", "number");
  protected final Measure mTopSQLCPUTime = mgOracleTopSQL.createMeasure("CPU Time", "CPU Time", "number");
  protected final Measure mTopSQLDiskReads = mgOracleTopSQL.createMeasure("Disk Reads", "Disk Reads", "number");
  protected final Measure mTopSQLDiskWrites = mgOracleTopSQL.createMeasure("Disk Writes", "Disk Writes", "number");
  protected final Measure mTopSQLExecutions = mgOracleTopSQL.createMeasure("Executions", "Executions", "number");
  protected final Measure mTopSQLParseCalls = mgOracleTopSQL.createMeasure("Parse Calls", "Parse Calls", "number");
  protected final Measure mTopSQLBufferGets = mgOracleTopSQL.createMeasure("Buffer Gets", "Buffer Gets", "number");
  protected final Measure mTopSQLRowsrocessed = mgOracleTopSQL.createMeasure("Rows rocessed", "Rows rocessed", "number");

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgOracle);
    groups.add(mgOracleTablespace);
    groups.add(mgOracleWaiter);
    groups.add(mgOracleLock);
    groups.add(mgOracleTopSQL);
  }

  @Override
  public void readConf() throws CommandException {
    super.readConf();
    config.setSQLType(DBConfig.TYPE_ORACLE);
    final String version = context.getConfigString(OracleMonitor.CONFIG_ORACLEVERSION, "");
    dbVariant = (version.contains("later") || version.contains("grather")) ? "_v11" : "";
  }

  @Override
  public void setup(final IContext context) throws CommandException {
    super.setup(context);
    checks = new SQLchecks[] {
        new SQLchecks(mgOracle, "/sql/oracle_metrics.sql"), //
        new SQLchecks(mgOracleTablespace, "/sql/oracle_tablespaces.sql", 2, "Tablespace"), //
        new SQLchecks(mgOracleWaiter, "/sql/oracle_waiters.sql", 2, "Waiter"), //
        new SQLchecks(mgOracleLock, "/sql/oracle_locks.sql", 2, "Lock"), //
        new SQLchecks(mgOracleTopSQL, "/sql/oracle{0}_topsql.sql", 2, "Query")//
    };
  }

}
