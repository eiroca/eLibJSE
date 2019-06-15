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

import net.eiroca.library.db.DBConfig;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.SQLchecks;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.IContext;

public class OracleMonitor extends DatabaseMonitor {

  private static final String CONFIG_ORACLEVERSION = "OracleVersion";

  //
  protected final MetricGroup mgOracle = new MetricGroup(mgMonitor, "Oracle Statistics", "Oracle - {0}");
  //
  protected final MetricGroup mgOracleStats = new MetricGroup(mgOracle, "Oracle Stats");
  protected final Measure mInfoSystemdate = mgOracleStats.createMeasure("Info - System date", "Info - System date", "number");
  protected final Measure mSGAFreeBufferWaits = mgOracleStats.createMeasure("SGA - Free Buffer Waits", "SGA - Free Buffer Waits", "number");
  protected final Measure mSGAWriteCompleteWaits = mgOracleStats.createMeasure("SGA - Write Complete Waits", "SGA - Write Complete Waits", "number");
  protected final Measure mSGABufferBusyWaits = mgOracleStats.createMeasure("SGA - Buffer Busy Waits", "SGA - Buffer Busy Waits", "number");
  protected final Measure mSGADBBlockChanges = mgOracleStats.createMeasure("SGA - DB Block Changes", "SGA - DB Block Changes", "number");
  protected final Measure mSGADBBlockGets = mgOracleStats.createMeasure("SGA - DB Block Gets", "SGA - DB Block Gets", "number");
  protected final Measure mSGAConsistentGets = mgOracleStats.createMeasure("SGA - Consistent Gets", "SGA - Consistent Gets", "number");
  protected final Measure mSGAPhysicalReads = mgOracleStats.createMeasure("SGA - Physical Reads", "SGA - Physical Reads", "number");
  protected final Measure mSGAPhysicalWrites = mgOracleStats.createMeasure("SGA - Physical Writes", "SGA - Physical Writes", "number");
  protected final Measure mSGABufferCacheHitRatio = mgOracleStats.createMeasure("SGA - Buffer Cache Hit Ratio", "SGA - Buffer Cache Hit Ratio", "percent");
  protected final Measure mSGAExecutionWithoutParseRatio = mgOracleStats.createMeasure("SGA - Execution Without Parse Ratio", "SGA - Execution Without Parse Ratio", "percent");
  protected final Measure mSGAMemorySortRatio = mgOracleStats.createMeasure("SGA - Memory Sort Ratio", "SGA - Memory Sort Ratio", "percent");
  protected final Measure mSessionsMaximumConcurrentUser = mgOracleStats.createMeasure("Sessions - Maximum Concurrent User", "Sessions - Maximum Concurrent User", "number");
  protected final Measure mSessionsCurrentConcurrentUser = mgOracleStats.createMeasure("Sessions - Current Concurrent User", "Sessions - Current Concurrent User", "number");
  protected final Measure mSessionsHighestConcurrentUser = mgOracleStats.createMeasure("Sessions - Highest Concurrent User", "Sessions - Highest Concurrent User", "number");
  protected final Measure mSessionsMaximumNamedUsers = mgOracleStats.createMeasure("Sessions - Maximum Named Users", "Sessions - Maximum Named Users", "number");
  protected final Measure mHitRatioSQLAreaGet = mgOracleStats.createMeasure("Hit Ratio - SQL Area Get", "Hit Ratio - SQL Area Get", "percent");
  protected final Measure mHitRatioSQLAreaPin = mgOracleStats.createMeasure("Hit Ratio - SQL Area Pin", "Hit Ratio - SQL Area Pin", "percent");
  protected final Measure mHitRatioTableProcedureGet = mgOracleStats.createMeasure("Hit Ratio - Table/Procedure Get", "Hit Ratio - Table/Procedure Get", "percent");
  protected final Measure mHitRatioTableProcedurePin = mgOracleStats.createMeasure("Hit Ratio - Table/Procedure Pin", "Hit Ratio - Table/Procedure Pin", "percent");
  protected final Measure mHitRatioBodyGet = mgOracleStats.createMeasure("Hit Ratio - Body Get", "Hit Ratio - Body Get", "percent");
  protected final Measure mHitRatioBodyPin = mgOracleStats.createMeasure("Hit Ratio - Body Pin", "Hit Ratio - Body Pin", "percent");
  protected final Measure mHitRatioTriggerGet = mgOracleStats.createMeasure("Hit Ratio - Trigger Get", "Hit Ratio - Trigger Get", "percent");
  protected final Measure mHitRatioTriggerPin = mgOracleStats.createMeasure("Hit Ratio - Trigger Pin", "Hit Ratio - Trigger Pin", "percent");
  protected final Measure mHitRatioLibraryCacheGet = mgOracleStats.createMeasure("Hit Ratio - Library Cache Get", "Hit Ratio - Library Cache Get", "percent");
  protected final Measure mHitRatioLibraryCachePin = mgOracleStats.createMeasure("Hit Ratio - Library Cache Pin", "Hit Ratio - Library Cache Pin", "percent");
  protected final Measure mHitRatioDictionaryCache = mgOracleStats.createMeasure("Hit Ratio - Dictionary Cache", "Hit Ratio - Dictionary Cache", "percent");
  protected final Measure mSharedPoolFreeMemory = mgOracleStats.createMeasure("Shared Pool - Free Memory", "Shared Pool - Free Memory", "number");
  protected final Measure mSharedPoolReloads = mgOracleStats.createMeasure("Shared Pool - Reloads", "Shared Pool - Reloads", "number");
  protected final Measure mLatchesWaitLatchGets = mgOracleStats.createMeasure("Latches - Wait Latch Gets", "Latches - Wait Latch Gets", "number");
  protected final Measure mLatchesImmediateLatchGets = mgOracleStats.createMeasure("Latches - Immediate Latch Gets", "Latches - Immediate Latch Gets", "number");
  protected final Measure mRedoSpaceWaitRatio = mgOracleStats.createMeasure("Redo - Space Wait Ratio", "Redo - Space Wait Ratio", "percent");
  protected final Measure mRedoAllocationLatch = mgOracleStats.createMeasure("Redo - Allocation Latch", "Redo - Allocation Latch", "number");
  protected final Measure mRedoCopyLatches = mgOracleStats.createMeasure("Redo - Copy Latches", "Redo - Copy Latches", "number");
  protected final Measure mInfoRecursiveCallsRatio = mgOracleStats.createMeasure("Info - Recursive Calls Ratio", "Info - Recursive Calls Ratio", "percent");
  protected final Measure mInfoShortTableScansRatio = mgOracleStats.createMeasure("Info - Short Table Scans Ratio", "Info - Short Table Scans Ratio", "percent");
  protected final Measure mInfoRollbackSegmentContention = mgOracleStats.createMeasure("Info - Rollback Segment Contention", "Info - Rollback Segment Contention", "number");
  protected final Measure mInfoCPUParseOverhead = mgOracleStats.createMeasure("Info - CPU Parse Overhead", "Info - CPU Parse Overhead", "number");
  protected final Measure mTableContentionChainedFetchRatio = mgOracleStats.createMeasure("Table Contention - Chained Fetch Ratio", "Table Contention - Chained Fetch Ratio", "percent");
  protected final Measure mTableContentionFreeListContention = mgOracleStats.createMeasure("Table Contention - Free List Contention", "Table Contention - Free List Contention", "number");
  //
  protected final MetricGroup mgOracleTablespace = new MetricGroup(mgOracle, "Oracle Tablespaces", "Tablespace - {0}");
  protected final Measure mTablespaceTotal = mgOracleTablespace.createMeasure("Total", "Tablespace - Total Space (MB)", "megabytes");
  protected final Measure mTablespaceUsed = mgOracleTablespace.createMeasure("Used", "Tablespace - Used Space (MB)", "megabytes");
  protected final Measure mTablespaceFree = mgOracleTablespace.createMeasure("Free", "Tablespace - Free Space (MB)", "megabytes");
  protected final Measure mTablespaceUsedPercent = mgOracleTablespace.createMeasure("Used %", "Tablespace - Used Space (%)", "percent");
  protected final Measure mTablespaceFreePercent = mgOracleTablespace.createMeasure("Free %", "Tablespace - Total Space (%)", "percent");
  //
  protected final MetricGroup mgOracleWaiter = new MetricGroup(mgOracle, "Oracle Waiters", "Waiter - {0}");
  protected final Measure mWaiterTotalWaits = mgOracleWaiter.createMeasure("Total Waits", "Waiter - Total Waits", "number");
  protected final Measure mWaiterTotalTimeouts = mgOracleWaiter.createMeasure("Total Timeouts", "Waiter - Total Timeouts", "number");
  protected final Measure mWaiterTimeWaited = mgOracleWaiter.createMeasure("Time Waited", "Waiter - Time Waited", "s");
  protected final Measure mWaiterAverageWaittime = mgOracleWaiter.createMeasure("Average Wait time", "Waiter - Average Wait time", "s");
  //
  protected final MetricGroup mgOracleLock = new MetricGroup(mgOracle, "Oracle Locks", "Lock - {0}");
  protected final Measure mLockLockMode = mgOracleLock.createMeasure("Lock Mode", "Lock - Lock Mode", "number");
  protected final Measure mLockLockStatus = mgOracleLock.createMeasure("Lock Status", "Lock - Lock Status", "number");
  protected final Measure mLockLocks = mgOracleLock.createMeasure("Locks", "Lock count", "number");
  //
  protected final MetricGroup mgOracleTopSQL = new MetricGroup(mgOracle, "Oracle TopSQLs", "TopSQL - {0}");
  protected final Measure mTopSQLElapsedTime = mgOracleTopSQL.createMeasure("Elapsed Time", "Elapsed Time", "number");
  protected final Measure mTopSQLCPUTime = mgOracleTopSQL.createMeasure("CPU Time", "CPU Time", "number");
  protected final Measure mTopSQLDiskReads = mgOracleTopSQL.createMeasure("Disk Reads", "Disk Reads", "number");
  protected final Measure mTopSQLDiskWrites = mgOracleTopSQL.createMeasure("Disk Writes", "Disk Writes", "number");
  protected final Measure mTopSQLExecutions = mgOracleTopSQL.createMeasure("Executions", "Executions", "number");
  protected final Measure mTopSQLParseCalls = mgOracleTopSQL.createMeasure("Parse Calls", "Parse Calls", "number");
  protected final Measure mTopSQLBufferGets = mgOracleTopSQL.createMeasure("Buffer Gets", "Buffer Gets", "number");
  protected final Measure mTopSQLRowsrocessed = mgOracleTopSQL.createMeasure("Rows rocessed", "Rows rocessed", "number");

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
        new SQLchecks(mgOracleStats, "/sql/oracle_metrics.sql"), //
        new SQLchecks(mgOracleTablespace, "/sql/oracle_tablespaces.sql", 2, "Tablespace"), //
        new SQLchecks(mgOracleWaiter, "/sql/oracle_waiters.sql", 2, "Waiter"), //
        new SQLchecks(mgOracleLock, "/sql/oracle_locks.sql", 2, "Lock"), //
        new SQLchecks(mgOracleTopSQL, "/sql/oracle{0}_topsql.sql", 2, "Query")//
    };
  }

}
