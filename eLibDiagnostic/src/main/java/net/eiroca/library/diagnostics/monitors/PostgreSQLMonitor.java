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

public class PostgreSQLMonitor extends DatabaseMonitor {

  protected final MetricGroup mgPostgreSQL = new MetricGroup("PostgreSQL Statistics", "PostgreSQL - {0}");
  protected final Measure mblk_read_time = mgPostgreSQL.createMeasure("blk_read_time", "blk_read_time", "number");
  protected final Measure mblk_write_time = mgPostgreSQL.createMeasure("blk_write_time", "blk_write_time", "number");
  protected final Measure mblks_hit = mgPostgreSQL.createMeasure("blks_hit", "blks_hit", "number");
  protected final Measure mblks_read = mgPostgreSQL.createMeasure("blks_read", "blks_read", "number");
  protected final Measure mconflicts = mgPostgreSQL.createMeasure("conflicts", "conflicts", "number");
  protected final Measure mdeadlocks = mgPostgreSQL.createMeasure("deadlocks", "deadlocks", "number");
  protected final Measure mheap_hit = mgPostgreSQL.createMeasure("heap_hit", "heap_hit", "number");
  protected final Measure mheap_ratio = mgPostgreSQL.createMeasure("heap_ratio", "heap_ratio", "number");
  protected final Measure mheap_read = mgPostgreSQL.createMeasure("heap_read", "heap_read", "number");
  protected final Measure midx_hit = mgPostgreSQL.createMeasure("idx_hit", "idx_hit", "number");
  protected final Measure midx_ratio = mgPostgreSQL.createMeasure("idx_ratio", "idx_ratio", "number");
  protected final Measure midx_read = mgPostgreSQL.createMeasure("idx_read", "idx_read", "number");
  protected final Measure mnumbackends = mgPostgreSQL.createMeasure("numbackends", "numbackends", "number");
  protected final Measure mstat_activity = mgPostgreSQL.createMeasure("stat_activity", "stat_activity", "number");
  protected final Measure mtemp_bytes = mgPostgreSQL.createMeasure("temp_bytes", "temp_bytes", "number");
  protected final Measure mtup_deleted = mgPostgreSQL.createMeasure("tup_deleted", "tup_deleted", "number");
  protected final Measure mtup_fetched = mgPostgreSQL.createMeasure("tup_fetched", "tup_fetched", "number");
  protected final Measure mtup_inserted = mgPostgreSQL.createMeasure("tup_inserted", "tup_inserted", "number");
  protected final Measure mtup_returned = mgPostgreSQL.createMeasure("tup_returned", "tup_returned", "number");
  protected final Measure mtup_updated = mgPostgreSQL.createMeasure("tup_updated", "tup_updated", "number");
  protected final Measure mxact_commit = mgPostgreSQL.createMeasure("xact_commit", "xact_commit", "number");
  protected final Measure mxact_rollback = mgPostgreSQL.createMeasure("xact_rollback", "xact_rollback", "number");

  protected final MetricGroup mgPostgreSQLTables = new MetricGroup("PostgreSQL Statistics", "PostgreSQL - Tables - {0}");
  protected final Measure mTables_analyze_count = mgPostgreSQLTables.createMeasure("Tables - analyze_count", "Tables - analyze_count", "number");
  protected final Measure mTables_autoanalyze_count = mgPostgreSQLTables.createMeasure("Tables - autoanalyze_count", "Tables - autoanalyze_count", "number");
  protected final Measure mTables_autovacuum_count = mgPostgreSQLTables.createMeasure("Tables - autovacuum_count", "Tables - autovacuum_count", "number");
  protected final Measure mTables_coalesce = mgPostgreSQLTables.createMeasure("Tables - coalesce", "Tables - coalesce", "number");
  protected final Measure mTables_heap_blks_hit = mgPostgreSQLTables.createMeasure("Tables - heap_blks_hit", "Tables - heap_blks_hit", "number");
  protected final Measure mTables_heap_blks_read = mgPostgreSQLTables.createMeasure("Tables - heap_blks_read", "Tables - heap_blks_read", "number");
  protected final Measure mTables_idx_blks_hit = mgPostgreSQLTables.createMeasure("Tables - idx_blks_hit", "Tables - idx_blks_hit", "number");
  protected final Measure mTables_idx_blks_read = mgPostgreSQLTables.createMeasure("Tables - idx_blks_read", "Tables - idx_blks_read", "number");
  protected final Measure mTables_n_dead_tup = mgPostgreSQLTables.createMeasure("Tables - n_dead_tup", "Tables - n_dead_tup", "number");
  protected final Measure mTables_n_live_tup = mgPostgreSQLTables.createMeasure("Tables - n_live_tup", "Tables - n_live_tup", "number");
  protected final Measure mTables_n_tup_del = mgPostgreSQLTables.createMeasure("Tables - n_tup_del", "Tables - n_tup_del", "number");
  protected final Measure mTables_n_tup_hot_upd = mgPostgreSQLTables.createMeasure("Tables - n_tup_hot_upd", "Tables - n_tup_hot_upd", "number");
  protected final Measure mTables_n_tup_ins = mgPostgreSQLTables.createMeasure("Tables - n_tup_ins", "Tables - n_tup_ins", "number");
  protected final Measure mTables_n_tup_upd = mgPostgreSQLTables.createMeasure("Tables - n_tup_upd", "Tables - n_tup_upd", "number");
  protected final Measure mTables_percent_of_times_index_used = mgPostgreSQLTables.createMeasure("Tables - percent_of_times_index_used", "Tables - percent_of_times_index_used", "number");
  protected final Measure mTables_rows_in_table = mgPostgreSQLTables.createMeasure("Tables - rows_in_table", "Tables - rows_in_table", "number");
  protected final Measure mTables_seq_scan = mgPostgreSQLTables.createMeasure("Tables - seq_scan", "Tables - seq_scan", "number");
  protected final Measure mTables_seq_tup_read = mgPostgreSQLTables.createMeasure("Tables - seq_tup_read", "Tables - seq_tup_read", "number");
  protected final Measure mTables_tidx_blks_hit = mgPostgreSQLTables.createMeasure("Tables - tidx_blks_hit", "Tables - tidx_blks_hit", "number");
  protected final Measure mTables_tidx_blks_read = mgPostgreSQLTables.createMeasure("Tables - tidx_blks_read", "Tables - tidx_blks_read", "number");
  protected final Measure mTables_toast_blks_hit = mgPostgreSQLTables.createMeasure("Tables - toast_blks_hit", "Tables - toast_blks_hit", "number");
  protected final Measure mTables_toast_blks_read = mgPostgreSQLTables.createMeasure("Tables - toast_blks_read", "Tables - toast_blks_read", "number");
  protected final Measure mTables_vacuum_count = mgPostgreSQLTables.createMeasure("Tables - vacuum_count", "Tables - vacuum_count", "number");

  protected final MetricGroup mgPostgreSQLIndexes = new MetricGroup("PostgreSQL Statistics", "PostgreSQL - Indexes - {0}");
  protected final Measure mIndexes_idx_blks_hit = mgPostgreSQLIndexes.createMeasure("idx_blks_hit", "Indexes - idx_blks_hit", "number");
  protected final Measure mIndexes_idx_blks_read = mgPostgreSQLIndexes.createMeasure("idx_blks_read", "Indexes - idx_blks_read", "number");
  protected final Measure mIndexes_idx_scan = mgPostgreSQLIndexes.createMeasure("idx_scan", "Indexes - idx_scan", "number");
  protected final Measure mIndexes_idx_tup_fetch = mgPostgreSQLIndexes.createMeasure("idx_tup_fetch", "Indexes - idx_tup_fetch", "number");
  protected final Measure mIndexes_idx_tup_read = mgPostgreSQLIndexes.createMeasure("idx_tup_read", "Indexes - idx_tup_read", "number");

  protected final MetricGroup mgPostgreSQLSequences = new MetricGroup("PostgreSQL Statistics", "PostgreSQL - Sequences - {0}");
  protected final Measure mSequences_blks_read = mgPostgreSQLSequences.createMeasure("blks_read", "blks_read", "number");
  protected final Measure mSequences_blks_hit = mgPostgreSQLSequences.createMeasure("blks_hit", "blks_hit", "number");

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgPostgreSQL);
    groups.add(mgPostgreSQLTables);
    groups.add(mgPostgreSQLIndexes);
    groups.add(mgPostgreSQLSequences);
  }

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
        new SQLchecks(mgPostgreSQL, "/sql/postgres_metrics.sql"), //
        new SQLchecks(mgPostgreSQLTables, "/sql/postgres_table.sql", 2, "Table"), //
        new SQLchecks(mgPostgreSQLIndexes, "/sql/postgres_index.sql", 2, "Index"), //
        new SQLchecks(mgPostgreSQLSequences, "/sql/postgres_sequence.sql", 2, "Sequence"), //
    };
  }

}
