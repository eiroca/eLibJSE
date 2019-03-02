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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.metrics.derived.HitMissRatioMeasure;
import net.eiroca.library.metrics.derived.RateMeasure;
import redis.clients.jedis.Jedis;

public class RedisMonitor extends TCPServerMonitor {

  public static final String CONFIG_PORT = "port";
  public static final String CONFIG_AUTH = "redisAuth";

  protected MetricGroup mgRedisInfo = new MetricGroup("Redis Statistics", "Redis - {0}");

  protected Measure mServerResult = mgServerInfo.createMeasure("Result");
  protected Measure mServerStatus = mgServerInfo.createMeasure("Status");

  protected Measure mRedisServerUptime = mgRedisInfo.createMeasure("Server - uptime");
  protected Measure mRedisStatsOps = mgRedisInfo.createMeasure("Stats - instantaneous_ops_per_sec");
  protected Measure mRedisStatsCmds = mgRedisInfo.createMeasure("Stats - total_commands_processed");
  protected Measure mRedisStatsHits = mgRedisInfo.createMeasure("Stats - keyspace_hits");
  protected Measure mRedisStatsMiss = mgRedisInfo.createMeasure("Stats - keyspace_misses");
  protected Measure mRedisClientsConn = mgRedisInfo.createMeasure("Clients - connected_clients");
  protected Measure mRedisMemoryUsed = mgRedisInfo.createMeasure("Memory - used_memory");
  protected Measure mRedisMemoryPeak = mgRedisInfo.createMeasure("Memory - used_memory_peak");
  protected Measure mRedisMemoryOverhead = mgRedisInfo.createMeasure("Memory - used_memory_overhead");
  protected Measure mRedisReplicationLastIO = mgRedisInfo.createMeasure("Replication - master_last_io_seconds_ago");
  protected Measure mRedisReplicationSyncIn = mgRedisInfo.createMeasure("Replication - master_sync_in_progress");
  protected Measure mRedisReplicationSlaves = mgRedisInfo.createMeasure("Replication - connected_slaves");
  protected Measure mRedisKeyspaceKeys = mgRedisInfo.createMeasure("Keyspace - keys");
  protected Measure mRedisRDBLstSave = mgRedisInfo.createMeasure("Persistence - rdb_last_bgsave_time_sec");
  protected Measure mRedisRDBBkgSave = mgRedisInfo.createMeasure("Persistence - rdb_bgsave_in_progress");
  protected Measure mRedisRDBChanges = mgRedisInfo.createMeasure("Persistence - rdb_changes_since_last_save");

  protected Measure mRedisHitRatio = mgRedisInfo.define("Keyspace Hit Ratio", new HitMissRatioMeasure(mRedisStatsHits, mRedisStatsMiss));
  protected Measure mRedisHitsPerSec = mgRedisInfo.define("Keyspace Hit Rate", new RateMeasure(mRedisStatsHits, TimeUnit.SECONDS, 0.0));
  protected Measure mRedisMissPerSec = mgRedisInfo.define("Keyspace Miss Rate", new RateMeasure(mRedisStatsMiss, TimeUnit.SECONDS, 0.0));

  protected Map<String, Measure> mapping = new HashMap<>();

  protected int port;
  protected String auth;

  public RedisMonitor() {
    super();
    mapping.put("uptime_in_seconds", mRedisServerUptime);
    mapping.put("instantaneous_ops_per_sec", mRedisStatsOps);
    mapping.put("total_commands_processed", mRedisStatsCmds);
    mapping.put("keyspace_hits", mRedisStatsHits);
    mapping.put("keyspace_misses", mRedisStatsMiss);
    mapping.put("connected_clients", mRedisClientsConn);
    mapping.put("used_memory", mRedisMemoryUsed);
    mapping.put("used_memory_peak", mRedisMemoryPeak);
    mapping.put("master_last_io_seconds_ago", mRedisReplicationLastIO);
    mapping.put("master_sync_in_progress", mRedisReplicationSyncIn);
    mapping.put("used_memory_overhead", mRedisMemoryOverhead);
    mapping.put("connected_slaves", mRedisReplicationSlaves);
    mapping.put("rdb_last_bgsave_time_sec", mRedisRDBLstSave);
    mapping.put("rdb_bgsave_in_progress", mRedisRDBBkgSave);
    mapping.put("rdb_changes_since_last_save", mRedisRDBChanges);
  }

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgRedisInfo);
  }

  @Override
  public void readConf() throws CommandException {
    port = context.getConfigInt(RedisMonitor.CONFIG_PORT, -1);
    if ((port < 0) || (port > 65535)) {
      CommandException.ConfigurationError("Invalid port number");
    }
    auth = context.getConfigPassword(RedisMonitor.CONFIG_AUTH);
    if (LibStr.isEmptyOrNull(auth)) {
      auth = null;
    }
  }

  @Override
  public boolean runCheck() throws CommandException {
    boolean succed = false;
    final long startTime = System.nanoTime();
    final long connectStartTime = startTime;
    long endTime = startTime;
    final boolean connectionTO = false;
    final double result = 0;
    final boolean error = getRedisInfo(targetHost.getHostString(), port, auth);
    succed = !error;
    endTime = System.nanoTime();
    mServerReachable.setValue(succed);
    mServerConnectionTimeout.setValue(connectionTO);
    mServerLatency.setValue(Helper.elapsed(startTime, connectStartTime));
    mServerResponseTime.setValue(Helper.elapsed(startTime, endTime));
    mServerResult.setValue(result);
    mServerStatus.setValue(error);
    return true;
  }

  public boolean getRedisInfo(final String host, final int port, final String auth) {
    boolean res = false;
    final Jedis jedis = new Jedis(host, port);
    try {
      if (auth != null) {
        jedis.auth(auth);
      }
      context.debug("Executing Redis INFO ");
      final String[] result = jedis.info().split("\\n");
      if (result.length > 0) {
        res = true;
        double numberOfKeys = 0;
        for (final String row : result) {
          if (LibStr.isEmptyOrNull(row) || (row.startsWith("#"))) {
            continue;
          }
          if (row.indexOf(':') > -1) {
            final String[] keyVal = row.split(":");
            context.trace("Processing key", keyVal[0]);
            final Measure m = mapping.get(keyVal[0]);
            if (m != null) {
              final Double value = Helper.getDouble(keyVal[1], -1);
              m.setValue(value);
              context.trace("Measure = ", m);
            }
            else {
              // # Keyspace
              // db0:keys=1299312,expires=1299312,avg_ttl=0
              // db3:keys=1862,expires=1862,avg_ttl=0
              if (keyVal[0].startsWith("db")) {
                numberOfKeys += Helper.getDouble(keyVal[1].split(",")[0].split("=")[1], 0);
              }
            }
          }
          mRedisKeyspaceKeys.setValue(numberOfKeys);
        }
      }
    }
    finally {
      jedis.close();
    }
    return res;
  }
}
