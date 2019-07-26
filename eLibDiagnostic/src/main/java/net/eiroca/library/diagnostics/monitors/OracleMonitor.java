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
package net.eiroca.library.diagnostics.monitors;

import net.eiroca.library.db.DBConfig;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.SQLchecks;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.IContext;

public class OracleMonitor extends DatabaseMonitor {

  private static final String CONFIG_ORACLEVERSION = "OracleVersion";

  //
  protected final MetricGroup mgOracle = new MetricGroup(mgMonitor, "Oracle Statistics", "Oracle - {0}");
  //
  protected final MetricGroup mgOracleStats = new MetricGroup(mgOracle, "Oracle Stats");
  protected final Measure mInfoSystemdate = mgOracleStats.createMeasure("Info - System date", MetricAggregation.zero, "Info - System date", "number");
  protected final Measure mSGAFreeBufferWaits = mgOracleStats.createMeasure("SGA - Free Buffer Waits", MetricAggregation.zero, "SGA - Free Buffer Waits", "number");
  protected final Measure mSGAWriteCompleteWaits = mgOracleStats.createMeasure("SGA - Write Complete Waits", MetricAggregation.zero, "SGA - Write Complete Waits", "number");
  protected final Measure mSGABufferBusyWaits = mgOracleStats.createMeasure("SGA - Buffer Busy Waits", MetricAggregation.zero, "SGA - Buffer Busy Waits", "number");
  protected final Measure mSGADBBlockChanges = mgOracleStats.createMeasure("SGA - DB Block Changes", MetricAggregation.zero, "SGA - DB Block Changes", "number");
  protected final Measure mSGADBBlockGets = mgOracleStats.createMeasure("SGA - DB Block Gets", MetricAggregation.zero, "SGA - DB Block Gets", "number");
  protected final Measure mSGAConsistentGets = mgOracleStats.createMeasure("SGA - Consistent Gets", MetricAggregation.zero, "SGA - Consistent Gets", "number");
  protected final Measure mSGAPhysicalReads = mgOracleStats.createMeasure("SGA - Physical Reads", MetricAggregation.zero, "SGA - Physical Reads", "number");
  protected final Measure mSGAPhysicalWrites = mgOracleStats.createMeasure("SGA - Physical Writes", MetricAggregation.zero, "SGA - Physical Writes", "number");
  protected final Measure mSGABufferCacheHitRatio = mgOracleStats.createMeasure("SGA - Buffer Cache Hit Ratio", MetricAggregation.zero, "SGA - Buffer Cache Hit Ratio", "percent");
  protected final Measure mSGAExecutionWithoutParseRatio = mgOracleStats.createMeasure("SGA - Execution Without Parse Ratio", MetricAggregation.zero, "SGA - Execution Without Parse Ratio", "percent");
  protected final Measure mSGAMemorySortRatio = mgOracleStats.createMeasure("SGA - Memory Sort Ratio", MetricAggregation.zero, "SGA - Memory Sort Ratio", "percent");
  protected final Measure mSessionsMaximumConcurrentUser = mgOracleStats.createMeasure("Sessions - Maximum Concurrent User", MetricAggregation.zero, "Sessions - Maximum Concurrent User", "number");
  protected final Measure mSessionsCurrentConcurrentUser = mgOracleStats.createMeasure("Sessions - Current Concurrent User", MetricAggregation.zero, "Sessions - Current Concurrent User", "number");
  protected final Measure mSessionsHighestConcurrentUser = mgOracleStats.createMeasure("Sessions - Highest Concurrent User", MetricAggregation.zero, "Sessions - Highest Concurrent User", "number");
  protected final Measure mSessionsMaximumNamedUsers = mgOracleStats.createMeasure("Sessions - Maximum Named Users", MetricAggregation.zero, "Sessions - Maximum Named Users", "number");
  protected final Measure mHitRatioSQLAreaGet = mgOracleStats.createMeasure("Hit Ratio - SQL Area Get", MetricAggregation.zero, "Hit Ratio - SQL Area Get", "percent");
  protected final Measure mHitRatioSQLAreaPin = mgOracleStats.createMeasure("Hit Ratio - SQL Area Pin", MetricAggregation.zero, "Hit Ratio - SQL Area Pin", "percent");
  protected final Measure mHitRatioTableProcedureGet = mgOracleStats.createMeasure("Hit Ratio - Table/Procedure Get", MetricAggregation.zero, "Hit Ratio - Table/Procedure Get", "percent");
  protected final Measure mHitRatioTableProcedurePin = mgOracleStats.createMeasure("Hit Ratio - Table/Procedure Pin", MetricAggregation.zero, "Hit Ratio - Table/Procedure Pin", "percent");
  protected final Measure mHitRatioBodyGet = mgOracleStats.createMeasure("Hit Ratio - Body Get", MetricAggregation.zero, "Hit Ratio - Body Get", "percent");
  protected final Measure mHitRatioBodyPin = mgOracleStats.createMeasure("Hit Ratio - Body Pin", MetricAggregation.zero, "Hit Ratio - Body Pin", "percent");
  protected final Measure mHitRatioTriggerGet = mgOracleStats.createMeasure("Hit Ratio - Trigger Get", MetricAggregation.zero, "Hit Ratio - Trigger Get", "percent");
  protected final Measure mHitRatioTriggerPin = mgOracleStats.createMeasure("Hit Ratio - Trigger Pin", MetricAggregation.zero, "Hit Ratio - Trigger Pin", "percent");
  protected final Measure mHitRatioLibraryCacheGet = mgOracleStats.createMeasure("Hit Ratio - Library Cache Get", MetricAggregation.zero, "Hit Ratio - Library Cache Get", "percent");
  protected final Measure mHitRatioLibraryCachePin = mgOracleStats.createMeasure("Hit Ratio - Library Cache Pin", MetricAggregation.zero, "Hit Ratio - Library Cache Pin", "percent");
  protected final Measure mHitRatioDictionaryCache = mgOracleStats.createMeasure("Hit Ratio - Dictionary Cache", MetricAggregation.zero, "Hit Ratio - Dictionary Cache", "percent");
  protected final Measure mSharedPoolFreeMemory = mgOracleStats.createMeasure("Shared Pool - Free Memory", MetricAggregation.zero, "Shared Pool - Free Memory", "number");
  protected final Measure mSharedPoolReloads = mgOracleStats.createMeasure("Shared Pool - Reloads", MetricAggregation.zero, "Shared Pool - Reloads", "number");
  protected final Measure mLatchesWaitLatchGets = mgOracleStats.createMeasure("Latches - Wait Latch Gets", MetricAggregation.zero, "Latches - Wait Latch Gets", "number");
  protected final Measure mLatchesImmediateLatchGets = mgOracleStats.createMeasure("Latches - Immediate Latch Gets", MetricAggregation.zero, "Latches - Immediate Latch Gets", "number");
  protected final Measure mRedoSpaceWaitRatio = mgOracleStats.createMeasure("Redo - Space Wait Ratio", MetricAggregation.zero, "Redo - Space Wait Ratio", "percent");
  protected final Measure mRedoAllocationLatch = mgOracleStats.createMeasure("Redo - Allocation Latch", MetricAggregation.zero, "Redo - Allocation Latch", "number");
  protected final Measure mRedoCopyLatches = mgOracleStats.createMeasure("Redo - Copy Latches", MetricAggregation.zero, "Redo - Copy Latches", "number");
  protected final Measure mInfoRecursiveCallsRatio = mgOracleStats.createMeasure("Info - Recursive Calls Ratio", MetricAggregation.zero, "Info - Recursive Calls Ratio", "percent");
  protected final Measure mInfoShortTableScansRatio = mgOracleStats.createMeasure("Info - Short Table Scans Ratio", MetricAggregation.zero, "Info - Short Table Scans Ratio", "percent");
  protected final Measure mInfoRollbackSegmentContention = mgOracleStats.createMeasure("Info - Rollback Segment Contention", MetricAggregation.zero, "Info - Rollback Segment Contention", "number");
  protected final Measure mInfoCPUParseOverhead = mgOracleStats.createMeasure("Info - CPU Parse Overhead", MetricAggregation.zero, "Info - CPU Parse Overhead", "number");
  protected final Measure mTableContentionChainedFetchRatio = mgOracleStats.createMeasure("Table Contention - Chained Fetch Ratio", MetricAggregation.zero, "Table Contention - Chained Fetch Ratio", "percent");
  protected final Measure mTableContentionFreeListContention = mgOracleStats.createMeasure("Table Contention - Free List Contention", MetricAggregation.zero, "Table Contention - Free List Contention", "number");
  //
  protected final MetricGroup mgOracleTablespace = new MetricGroup(mgOracle, "Oracle Tablespaces", "Tablespace - {0}");
  protected final Measure mTablespaceTotal = mgOracleTablespace.createMeasure("Total", MetricAggregation.sum, "Tablespace - Total Space (MB)", "megabytes");
  protected final Measure mTablespaceUsed = mgOracleTablespace.createMeasure("Used", MetricAggregation.sum, "Tablespace - Used Space (MB)", "megabytes");
  protected final Measure mTablespaceFree = mgOracleTablespace.createMeasure("Free", MetricAggregation.sum, "Tablespace - Free Space (MB)", "megabytes");
  protected final Measure mTablespaceUsedPercent = mgOracleTablespace.createMeasure("Used %", MetricAggregation.zero, "Tablespace - Used Space (%)", "percent");
  protected final Measure mTablespaceFreePercent = mgOracleTablespace.createMeasure("Free %", MetricAggregation.zero, "Tablespace - Total Space (%)", "percent");
  //
  protected final MetricGroup mgOracleWaiter = new MetricGroup(mgOracle, "Oracle Waiters", "Waiter - {0}");
  protected final Measure mWaiterTotalWaits = mgOracleWaiter.createMeasure("Total Waits", MetricAggregation.sum, "Waiter - Total Waits", "number");
  protected final Measure mWaiterTotalTimeouts = mgOracleWaiter.createMeasure("Total Timeouts", MetricAggregation.sum, "Waiter - Total Timeouts", "number");
  protected final Measure mWaiterTimeWaited = mgOracleWaiter.createMeasure("Time Waited", MetricAggregation.sum, "Waiter - Time Waited", "s");
  protected final Measure mWaiterAverageWaittime = mgOracleWaiter.createMeasure("Average Wait time", MetricAggregation.sum, "Waiter - Average Wait time", "s");
  //
  protected final MetricGroup mgOracleLock = new MetricGroup(mgOracle, "Oracle Locks", "Lock - {0}");
  protected final Measure mLockLockMode = mgOracleLock.createMeasure("Lock Mode", MetricAggregation.zero, "Type of the Lock:   0, 'None'; 1, 'Null'; 2, 'Row-S (SS)'; 3, 'Row-X (SX)'; 4, 'Share'; 5, 'S/Row-X (SSX)'; 6, 'Exclusive'", "number");
  protected final Measure mLockLockStatus = mgOracleLock.createMeasure("Lock Status", MetricAggregation.zero, "Status of the database object", "number");
  protected final Measure mLockLocks = mgOracleLock.createMeasure("Locks", MetricAggregation.sum, "Lock count", "number");
  //
  protected final MetricGroup mgOracleTopSQL = new MetricGroup(mgOracle, "Oracle TopSQLs", "TopSQL - {0}");
  protected final Measure mTopSQLElapsedTime = mgOracleTopSQL.createMeasure("Elapsed Time", MetricAggregation.zero, "Elapsed Time", "number");
  protected final Measure mTopSQLCPUTime = mgOracleTopSQL.createMeasure("CPU Time", MetricAggregation.zero, "CPU Time", "number");
  protected final Measure mTopSQLDiskReads = mgOracleTopSQL.createMeasure("Disk Reads", MetricAggregation.zero, "Disk Reads", "number");
  protected final Measure mTopSQLDiskWrites = mgOracleTopSQL.createMeasure("Disk Writes", MetricAggregation.zero, "Disk Writes", "number");
  protected final Measure mTopSQLExecutions = mgOracleTopSQL.createMeasure("Executions", MetricAggregation.zero, "Executions", "number");
  protected final Measure mTopSQLParseCalls = mgOracleTopSQL.createMeasure("Parse Calls", MetricAggregation.zero, "Parse Calls", "number");
  protected final Measure mTopSQLBufferGets = mgOracleTopSQL.createMeasure("Buffer Gets", MetricAggregation.zero, "Buffer Gets", "number");
  protected final Measure mTopSQLRowsrocessed = mgOracleTopSQL.createMeasure("Rows rocessed", MetricAggregation.zero, "Rows rocessed", "number");

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
