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

import static net.eiroca.library.metrics.MetricAggregation.zero;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricAggregation;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.metrics.derived.HitMissRatioMeasure;
import net.eiroca.library.metrics.derived.RateMeasure;
import redis.clients.jedis.Jedis;

public class RedisMonitor extends TCPServerMonitor {

  public static final String CONFIG_PORT = "port";
  public static final String CONFIG_AUTH = "redisAuth";

  protected MetricGroup mgRedisInfo = new MetricGroup(mgMonitor, "Redis Statistics");
  protected Measure mRedisClientsConn = mgRedisInfo.createMeasure("Clients - connected clients", MetricAggregation.zero, "Connected clients", "number");
  protected Measure mRedisKeyspaceKeys = mgRedisInfo.createMeasure("Keyspace - keys", MetricAggregation.zero, "Keyspace - keys", "number");
  protected Measure mRedisMemoryOverhead = mgRedisInfo.createMeasure("Memory - used memory overhead", MetricAggregation.zero, "Memory - used memory overhead", "bytes");
  protected Measure mRedisMemoryPeak = mgRedisInfo.createMeasure("Memory - used memory peak", MetricAggregation.zero, "Memory - used memory peak", "bytes");
  protected Measure mRedisMemoryUsed = mgRedisInfo.createMeasure("Memory - used memory", MetricAggregation.zero, "Memory - used memory", "bytes");
  protected Measure mRedisRDBBkgSave = mgRedisInfo.createMeasure("Persistence - rdb bgsave in progress", MetricAggregation.zero, "Persistence - rdb bgsave in progress", "number");
  protected Measure mRedisRDBChanges = mgRedisInfo.createMeasure("Persistence - rdb changes since last save", MetricAggregation.zero, "Persistence - rdb changes since last save", "number");
  protected Measure mRedisRDBLstSave = mgRedisInfo.createMeasure("Persistence - rdb last bgsave time sec", MetricAggregation.zero, "Persistence - rdb last bgsave time sec", "s");
  protected Measure mRedisReplicationLastIO = mgRedisInfo.createMeasure("Replication - master last io seconds ago", MetricAggregation.zero, "Replication - master last io seconds ago", "s");
  protected Measure mRedisReplicationSlaves = mgRedisInfo.createMeasure("Replication - connected slaves", MetricAggregation.zero, "Replication - connected slaves", "number");
  protected Measure mRedisReplicationSyncIn = mgRedisInfo.createMeasure("Replication - master sync in progress", MetricAggregation.zero, "Replication - master sync in progress", "number");
  protected Measure mRedisServerUptime = mgRedisInfo.createMeasure("Server - uptime", MetricAggregation.zero, "Server uptime", "s");
  protected Measure mRedisStatsCmds = mgRedisInfo.createMeasure("Stats - total commands processed", MetricAggregation.zero, "Total commands processed", "number");
  protected Measure mRedisStatsHits = mgRedisInfo.createMeasure("Stats - keyspace hits", MetricAggregation.zero, "Keyspace hits", "number");
  protected Measure mRedisStatsMiss = mgRedisInfo.createMeasure("Stats - keyspace misses", MetricAggregation.zero, "Keyspace misses", "number");
  protected Measure mRedisStatsOps = mgRedisInfo.createMeasure("Stats - instantaneous ops per sec", MetricAggregation.zero, "Instantaneous operations per second", "number", "s");
  protected Measure mRedisHitRatio = mgRedisInfo.define("Keyspace Hit Ratio", new HitMissRatioMeasure(mRedisStatsHits, mRedisStatsMiss), MetricAggregation.zero, "Keyspace Hit Ratio", "percent", (String)null);
  protected Measure mRedisHitsPerSec = mgRedisInfo.define("Keyspace Hit Rate", new RateMeasure(mRedisStatsHits, TimeUnit.SECONDS, 0.0), MetricAggregation.zero, "Keyspace Hit Rate", "number", "s");
  protected Measure mRedisMissPerSec = mgRedisInfo.define("Keyspace Miss Rate", new RateMeasure(mRedisStatsMiss, TimeUnit.SECONDS, 0.0), zero, "Keyspace Miss Rate", "number", "s");

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
