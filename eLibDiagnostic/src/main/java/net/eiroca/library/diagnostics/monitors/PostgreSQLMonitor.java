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
package net.eiroca.library.diagnostics.monitors;

import net.eiroca.library.db.DBConfig;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.SQLchecks;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.IContext;

public class PostgreSQLMonitor extends DatabaseMonitor {

  //
  protected final MetricGroup mgPostgreSQL = new MetricGroup(mgMonitor, "PostgreSQL Statistics", "PostgreSQL - {0}");
  //
  protected final MetricGroup mgPostgreSQLStats = new MetricGroup(mgPostgreSQL, "PostgreSQL Stats");
  protected final Measure mblk_read_time = mgPostgreSQLStats.createMeasure("blk_read_time", MetricAggregation.zero, "blk_read_time", "number");
  protected final Measure mblk_write_time = mgPostgreSQLStats.createMeasure("blk_write_time", MetricAggregation.zero, "blk_write_time", "number");
  protected final Measure mblks_hit = mgPostgreSQLStats.createMeasure("blks_hit", MetricAggregation.zero, "blks_hit", "number");
  protected final Measure mblks_read = mgPostgreSQLStats.createMeasure("blks_read", MetricAggregation.zero, "blks_read", "number");
  protected final Measure mconflicts = mgPostgreSQLStats.createMeasure("conflicts", MetricAggregation.zero, "conflicts", "number");
  protected final Measure mdeadlocks = mgPostgreSQLStats.createMeasure("deadlocks", MetricAggregation.zero, "deadlocks", "number");
  protected final Measure mheap_hit = mgPostgreSQLStats.createMeasure("heap_hit", MetricAggregation.zero, "heap_hit", "number");
  protected final Measure mheap_ratio = mgPostgreSQLStats.createMeasure("heap_ratio", MetricAggregation.zero, "heap_ratio", "number");
  protected final Measure mheap_read = mgPostgreSQLStats.createMeasure("heap_read", MetricAggregation.zero, "heap_read", "number");
  protected final Measure midx_hit = mgPostgreSQLStats.createMeasure("idx_hit", MetricAggregation.zero, "idx_hit", "number");
  protected final Measure midx_ratio = mgPostgreSQLStats.createMeasure("idx_ratio", MetricAggregation.zero, "idx_ratio", "number");
  protected final Measure midx_read = mgPostgreSQLStats.createMeasure("idx_read", MetricAggregation.zero, "idx_read", "number");
  protected final Measure mnumbackends = mgPostgreSQLStats.createMeasure("numbackends", MetricAggregation.zero, "numbackends", "number");
  protected final Measure mstat_activity = mgPostgreSQLStats.createMeasure("stat_activity", MetricAggregation.zero, "stat_activity", "number");
  protected final Measure mtemp_bytes = mgPostgreSQLStats.createMeasure("temp_bytes", MetricAggregation.zero, "temp_bytes", "number");
  protected final Measure mtup_deleted = mgPostgreSQLStats.createMeasure("tup_deleted", MetricAggregation.zero, "tup_deleted", "number");
  protected final Measure mtup_fetched = mgPostgreSQLStats.createMeasure("tup_fetched", MetricAggregation.zero, "tup_fetched", "number");
  protected final Measure mtup_inserted = mgPostgreSQLStats.createMeasure("tup_inserted", MetricAggregation.zero, "tup_inserted", "number");
  protected final Measure mtup_returned = mgPostgreSQLStats.createMeasure("tup_returned", MetricAggregation.zero, "tup_returned", "number");
  protected final Measure mtup_updated = mgPostgreSQLStats.createMeasure("tup_updated", MetricAggregation.zero, "tup_updated", "number");
  protected final Measure mxact_commit = mgPostgreSQLStats.createMeasure("xact_commit", MetricAggregation.zero, "xact_commit", "number");
  protected final Measure mxact_rollback = mgPostgreSQLStats.createMeasure("xact_rollback", MetricAggregation.zero, "xact_rollback", "number");
  //
  protected final MetricGroup mgPostgreSQLTables = new MetricGroup(mgPostgreSQL, "PostgreSQL Tables", "Tables - {0}");
  protected final Measure mTables_analyze_count = mgPostgreSQLTables.createMeasure("analyze_count", MetricAggregation.zero, "Tables - analyze_count", "number");
  protected final Measure mTables_autoanalyze_count = mgPostgreSQLTables.createMeasure("autoanalyze_count", MetricAggregation.zero, "Tables - autoanalyze_count", "number");
  protected final Measure mTables_autovacuum_count = mgPostgreSQLTables.createMeasure("autovacuum_count", MetricAggregation.zero, "Tables - autovacuum_count", "number");
  protected final Measure mTables_coalesce = mgPostgreSQLTables.createMeasure("coalesce", MetricAggregation.zero, "Tables - coalesce", "number");
  protected final Measure mTables_heap_blks_hit = mgPostgreSQLTables.createMeasure("heap_blks_hit", MetricAggregation.zero, "Tables - heap_blks_hit", "number");
  protected final Measure mTables_heap_blks_read = mgPostgreSQLTables.createMeasure("heap_blks_read", MetricAggregation.zero, "Tables - heap_blks_read", "number");
  protected final Measure mTables_idx_blks_hit = mgPostgreSQLTables.createMeasure("idx_blks_hit", MetricAggregation.zero, "Tables - idx_blks_hit", "number");
  protected final Measure mTables_idx_blks_read = mgPostgreSQLTables.createMeasure("idx_blks_read", MetricAggregation.zero, "Tables - idx_blks_read", "number");
  protected final Measure mTables_n_dead_tup = mgPostgreSQLTables.createMeasure("n_dead_tup", MetricAggregation.zero, "Tables - n_dead_tup", "number");
  protected final Measure mTables_n_live_tup = mgPostgreSQLTables.createMeasure("n_live_tup", MetricAggregation.zero, "Tables - n_live_tup", "number");
  protected final Measure mTables_n_tup_del = mgPostgreSQLTables.createMeasure("n_tup_del", MetricAggregation.zero, "Tables - n_tup_del", "number");
  protected final Measure mTables_n_tup_hot_upd = mgPostgreSQLTables.createMeasure("n_tup_hot_upd", MetricAggregation.zero, "Tables - n_tup_hot_upd", "number");
  protected final Measure mTables_n_tup_ins = mgPostgreSQLTables.createMeasure("n_tup_ins", MetricAggregation.zero, "Tables - n_tup_ins", "number");
  protected final Measure mTables_n_tup_upd = mgPostgreSQLTables.createMeasure("n_tup_upd", MetricAggregation.zero, "Tables - n_tup_upd", "number");
  protected final Measure mTables_percent_of_times_index_used = mgPostgreSQLTables.createMeasure("percent_of_times_index_used", MetricAggregation.zero, "Tables - percent_of_times_index_used", "number");
  protected final Measure mTables_rows_in_table = mgPostgreSQLTables.createMeasure("rows_in_table", MetricAggregation.zero, "Tables - rows_in_table", "number");
  protected final Measure mTables_seq_scan = mgPostgreSQLTables.createMeasure("seq_scan", MetricAggregation.zero, "Tables - seq_scan", "number");
  protected final Measure mTables_seq_tup_read = mgPostgreSQLTables.createMeasure("seq_tup_read", MetricAggregation.zero, "Tables - seq_tup_read", "number");
  protected final Measure mTables_tidx_blks_hit = mgPostgreSQLTables.createMeasure("tidx_blks_hit", MetricAggregation.zero, "Tables - tidx_blks_hit", "number");
  protected final Measure mTables_tidx_blks_read = mgPostgreSQLTables.createMeasure("tidx_blks_read", MetricAggregation.zero, "Tables - tidx_blks_read", "number");
  protected final Measure mTables_toast_blks_hit = mgPostgreSQLTables.createMeasure("toast_blks_hit", MetricAggregation.zero, "Tables - toast_blks_hit", "number");
  protected final Measure mTables_toast_blks_read = mgPostgreSQLTables.createMeasure("toast_blks_read", MetricAggregation.zero, "Tables - toast_blks_read", "number");
  protected final Measure mTables_vacuum_count = mgPostgreSQLTables.createMeasure("vacuum_count", MetricAggregation.zero, "Tables - vacuum_count", "number");
  //
  protected final MetricGroup mgPostgreSQLIndexes = new MetricGroup(mgPostgreSQL, "PostgreSQL Indexes", "Indexes - {0}");
  protected final Measure mIndexes_idx_blks_hit = mgPostgreSQLIndexes.createMeasure("idx_blks_hit", MetricAggregation.zero, "Indexes - idx_blks_hit", "number");
  protected final Measure mIndexes_idx_blks_read = mgPostgreSQLIndexes.createMeasure("idx_blks_read", MetricAggregation.zero, "Indexes - idx_blks_read", "number");
  protected final Measure mIndexes_idx_scan = mgPostgreSQLIndexes.createMeasure("idx_scan", MetricAggregation.zero, "Indexes - idx_scan", "number");
  protected final Measure mIndexes_idx_tup_fetch = mgPostgreSQLIndexes.createMeasure("idx_tup_fetch", MetricAggregation.zero, "Indexes - idx_tup_fetch", "number");
  protected final Measure mIndexes_idx_tup_read = mgPostgreSQLIndexes.createMeasure("idx_tup_read", MetricAggregation.zero, "Indexes - idx_tup_read", "number");
  //
  protected final MetricGroup mgPostgreSQLSequences = new MetricGroup(mgPostgreSQL, "PostgreSQL Sequences", "Sequences - {0}");
  protected final Measure mSequences_blks_read = mgPostgreSQLSequences.createMeasure("blks_read", MetricAggregation.zero, "blks_read", "number");
  protected final Measure mSequences_blks_hit = mgPostgreSQLSequences.createMeasure("blks_hit", MetricAggregation.zero, "blks_hit", "number");

  @Override
  public void readConf() throws CommandException {
    super.readConf();
    config.setSQLType(DBConfig.TYPE_POSTGRES);
    sqlParams = new String[] {
        config.getDatabase()
    };
  }

  @Override
  public void setup(final IContext context) throws CommandException {
    super.setup(context);
    checks = new SQLchecks[] {
        new SQLchecks(mgPostgreSQLStats, "/sql/postgres_metrics.sql"), //
        new SQLchecks(mgPostgreSQLTables, "/sql/postgres_table.sql", 2, "Table"), //
        new SQLchecks(mgPostgreSQLIndexes, "/sql/postgres_index.sql", 2, "Index"), //
        new SQLchecks(mgPostgreSQLSequences, "/sql/postgres_sequence.sql", 2, "Sequence"), //
    };
  }

}
