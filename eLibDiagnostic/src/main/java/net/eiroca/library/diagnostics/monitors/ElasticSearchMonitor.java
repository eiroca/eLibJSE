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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.metrics.derived.RateMeasure;

public class ElasticSearchMonitor extends GenericHTTPMonitor {

  private static final String ELASTIC_CLUSTER_HEALTH = "_cluster/health";
  private static final String ELASTIC_CLUSTER_STATS = "_cluster/stats";
  private static final String ELASTIC_NODES = "_nodes";
  private static final String ELASTIC_NODES_STATS = "_nodes/stats";

  private static final String SPLIT_NODE = "Node";
  private static final String SPLIT_STAT = "Stat";
  private static final String SPLIT_STATE = "State";

  protected static final String CONFIG_ELASTICURL = "elasticURL";

  MetricGroup mgElasticSearch = new MetricGroup("ElasticSearch Statistics", "ElasticSearch - {0}");

  Measure mActivePrimaryShards = mgElasticSearch.createMeasure("ActivePrimaryShards");
  Measure mActiveShards = mgElasticSearch.createMeasure("ActiveShards");
  Measure mActiveShardsPercent = mgElasticSearch.createMeasure("ActiveShardsPercent");
  Measure mCompletionSize = mgElasticSearch.createMeasure("CompletionSize");
  Measure mDataNodeCount = mgElasticSearch.createMeasure("DataNodeCount");
  Measure mDelayedUnassignedShards = mgElasticSearch.createMeasure("DelayedUnassignedShards");
  Measure mDeleteCurrent = mgElasticSearch.createMeasure("DeleteCurrent");
  Measure mDeletedCount = mgElasticSearch.createMeasure("DeletedCount");
  Measure mDocumentDeletedRate = mgElasticSearch.define("DeletedCountPerSecond", new RateMeasure(mDeletedCount, TimeUnit.SECONDS, 0.0));
  Measure mDocumentCount = mgElasticSearch.createMeasure("DocCount");
  Measure mDocumentRate = mgElasticSearch.define("DocCountPerSecond", new RateMeasure(mDocumentCount, TimeUnit.SECONDS, 0.0));
  Measure mFetchCurrent = mgElasticSearch.createMeasure("FetchCurrent");
  Measure mFieldDataEvictions = mgElasticSearch.createMeasure("FieldDataEvictions");
  Measure mFiledDataSize = mgElasticSearch.createMeasure("FieldDataSize");
  Measure mFileDescLimitPerNode = mgElasticSearch.createMeasure("FileDescriptorLimit");
  Measure mFileDescriptorCount = mgElasticSearch.createMeasure("FileDescriptorCount");
  Measure mFileSystemSize = mgElasticSearch.createMeasure("FileSystemSize");
  Measure mIndexCount = mgElasticSearch.createMeasure("IndexCount");
  Measure mIndexingCurrent = mgElasticSearch.createMeasure("IndexingCurrent");
  Measure mIndexingThrottleTime = mgElasticSearch.createMeasure("IndexingThrottleTime");
  Measure mInitializingShards = mgElasticSearch.createMeasure("InitializingShards");
  Measure mMemInitHeap = mgElasticSearch.createMeasure("InitHeap");
  Measure mMemInitNonHeap = mgElasticSearch.createMeasure("InitNonHeap");
  Measure mMemMaxDirect = mgElasticSearch.createMeasure("MaxDirect");
  Measure mMemMaxHeap = mgElasticSearch.createMeasure("MaxHeap");
  Measure mMemMaxNonHeap = mgElasticSearch.createMeasure("MaxNonHeap");
  Measure mNodeCount = mgElasticSearch.createMeasure("NodeCount");
  Measure mPercolateCount = mgElasticSearch.createMeasure("PercolateCount");
  Measure mPercolateSize = mgElasticSearch.createMeasure("PercolateSize");
  Measure mQueryCacheSize = mgElasticSearch.createMeasure("QueryCacheSize");
  Measure mQueryCurrent = mgElasticSearch.createMeasure("QueryCurrent");
  Measure mRecoveryAsSource = mgElasticSearch.createMeasure("RecoveryAsSource");
  Measure mRecoveryAsTarget = mgElasticSearch.createMeasure("RecoveryAsTarget");
  Measure mRecoveryThrottleTime = mgElasticSearch.createMeasure("RecoveryThrottleTime");
  Measure mRelocatingShards = mgElasticSearch.createMeasure("RelocatingShards");
  Measure mRequestCacheSize = mgElasticSearch.createMeasure("RequestCacheSize");
  Measure mScrollCurrent = mgElasticSearch.createMeasure("ScrollCurrent");
  Measure mSegmentCount = mgElasticSearch.createMeasure("SegmentCount");
  Measure mSegmentSize = mgElasticSearch.createMeasure("SegmentSize");
  Measure mShardCount = mgElasticSearch.createMeasure("ShardCount");
  Measure mStoreSize = mgElasticSearch.createMeasure("StoreSize");
  Measure mStoreThrottleTime = mgElasticSearch.createMeasure("StoreThrottleTime");
  Measure mTranslogSize = mgElasticSearch.createMeasure("TranslogSize");
  Measure mUnassignedShards = mgElasticSearch.createMeasure("UnassignedShards");

  String baseURL;

  @Override
  public void loadMetricGroup(final List<MetricGroup> groups) {
    super.loadMetricGroup(groups);
    groups.add(mgElasticSearch);
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    final boolean ok = super.preCheck(host);
    if (ok) {
      baseURL = LibStr.fixPathWithSlash(getURL(ElasticSearchMonitor.CONFIG_ELASTICURL, host.getHostName()).toString());
      fetcher.setMethod(URLFetcherConfig.METHOD_GET, null);
    }
    context.info("ElasticSearch URL: " + baseURL);
    return ok;
  }

  @Override
  public ReturnObject fetchResponse() throws CommandException {
    retrieveClusterState();
    retrieveClusterHealth();
    retrieveNodeHealth();
    retrieveNodeStats();
    context.debug(mgElasticSearch);
    return null;
  }

  public URL elasticURL(final String service) {
    try {
      return new URL(baseURL + service);
    }
    catch (final MalformedURLException e) {
      context.error("Invalid URL: " + baseURL + service);
    }
    return null;
  }

  private static void setValue(final Measure measure, final JSONObject node, final String key) {
    if ((node != null) && (node.has(key))) {
      measure.setValue(node.getDouble(key));
    }
  }

  private static void addValue(final Measure measure, final JSONObject node, final String key) {
    if ((node != null) && (node.has(key))) {
      measure.addValue(node.getDouble(key));
    }
  }

  private static void addValue(final Measure measure, final String split, final String splitName, final JSONObject node, final String key) {
    if ((node != null) && (node.has(key))) {
      measure.getSplitting(split, splitName).addValue(node.getDouble(key));
    }
  }

  private static void addValue(final Measure measure, final String split, final JSONObject node, final String key) {
    if ((node != null) && (node.has(key))) {
      measure.getSplitting(split, key).addValue(node.getDouble(key));
    }
  }

  private JSONObject get(final String service) {
    fetcher.setURL(elasticURL(service));
    final ReturnObject response = httpCall(fetcher);
    final String jsonStr = response.getOutput();
    context.debug(service + " -> " + jsonStr);
    return new JSONObject(jsonStr);
  }

  private void retrieveClusterState() {
    final JSONObject clusterStats = get(ElasticSearchMonitor.ELASTIC_CLUSTER_STATS);
    final JSONObject index = clusterStats.optJSONObject("indices");
    ElasticSearchMonitor.setValue(mIndexCount, index, "count");
    final JSONObject shards = index.optJSONObject("shards");
    ElasticSearchMonitor.setValue(mShardCount, shards, "total");
    ElasticSearchMonitor.addValue(mShardCount, ElasticSearchMonitor.SPLIT_STATE, "primary", shards, "primaries");
    ElasticSearchMonitor.addValue(mShardCount, ElasticSearchMonitor.SPLIT_STATE, "replicationFactor", shards, "replication");
    final JSONObject docs = index.optJSONObject("docs");
    ElasticSearchMonitor.setValue(mDocumentCount, docs, "count");
    ElasticSearchMonitor.setValue(mDeletedCount, docs, "deleted");
    final JSONObject store = index.optJSONObject("store");
    ElasticSearchMonitor.setValue(mStoreThrottleTime, store, "throttle_time_in_millis");
    ElasticSearchMonitor.setValue(mStoreSize, store, "size_in_bytes");
    final JSONObject fielddata = index.optJSONObject("fielddata");
    ElasticSearchMonitor.setValue(mFiledDataSize, fielddata, "memory_size_in_bytes");
    ElasticSearchMonitor.setValue(mFieldDataEvictions, fielddata, "evictions");
    final JSONObject query_cache = index.optJSONObject("query_cache");
    ElasticSearchMonitor.setValue(mQueryCacheSize, query_cache, "memory_size_in_bytes");
    ElasticSearchMonitor.addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "total_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "hit_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "miss_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "cache_size");
    ElasticSearchMonitor.addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "cache_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "evictions");
    final JSONObject completion = index.optJSONObject("completion");
    ElasticSearchMonitor.setValue(mCompletionSize, completion, "size_in_bytes");
    final JSONObject segments = index.optJSONObject("segments");
    ElasticSearchMonitor.setValue(mSegmentCount, segments, "count");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "count");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "terms_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "stored_fields_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "term_vectors_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "norms_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "doc_values_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "index_writer_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "index_writer_max_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "version_map_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "fixed_bit_set_memory_in_bytes");
    final JSONObject percolate = index.optJSONObject("percolate");
    ElasticSearchMonitor.setValue(mPercolateCount, percolate, "current");
    ElasticSearchMonitor.addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "total");
    ElasticSearchMonitor.addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "time_in_millis");
    ElasticSearchMonitor.addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "current");
    ElasticSearchMonitor.addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "memory_size_in_bytes");
    ElasticSearchMonitor.addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "queries");
    final JSONObject nodes = clusterStats.optJSONObject("nodes");
    if (nodes != null) {
      final JSONObject process = nodes.optJSONObject("process");
      if (process != null) {
        final JSONObject fileDesc = process.optJSONObject("open_file_descriptors");
        ElasticSearchMonitor.setValue(mFileDescriptorCount, fileDesc, "max");
        ElasticSearchMonitor.addValue(mFileDescriptorCount, ElasticSearchMonitor.SPLIT_STAT, fileDesc, "min");
        ElasticSearchMonitor.addValue(mFileDescriptorCount, ElasticSearchMonitor.SPLIT_STAT, fileDesc, "max");
        ElasticSearchMonitor.addValue(mFileDescriptorCount, ElasticSearchMonitor.SPLIT_STAT, fileDesc, "avg");
      }
      final JSONObject fs = nodes.optJSONObject("fs");
      ElasticSearchMonitor.addValue(mFileSystemSize, fs, "free_in_bytes");
      ElasticSearchMonitor.addValue(mFileSystemSize, ElasticSearchMonitor.SPLIT_STAT, fs, "total_in_bytes");
      ElasticSearchMonitor.addValue(mFileSystemSize, ElasticSearchMonitor.SPLIT_STAT, fs, "free_in_bytes");
      ElasticSearchMonitor.addValue(mFileSystemSize, ElasticSearchMonitor.SPLIT_STAT, fs, "available_in_bytes");
    }
  }

  private void retrieveClusterHealth() {
    final JSONObject clusterHealth = get(ElasticSearchMonitor.ELASTIC_CLUSTER_HEALTH);
    ElasticSearchMonitor.setValue(mNodeCount, clusterHealth, "number_of_nodes");
    ElasticSearchMonitor.setValue(mDataNodeCount, clusterHealth, "number_of_data_nodes");
    ElasticSearchMonitor.setValue(mActivePrimaryShards, clusterHealth, "active_primary_shards");
    ElasticSearchMonitor.setValue(mActiveShardsPercent, clusterHealth, "active_shards_percent_as_number");
    ElasticSearchMonitor.setValue(mActiveShards, clusterHealth, "active_shards");
    ElasticSearchMonitor.setValue(mRelocatingShards, clusterHealth, "relocating_shards");
    ElasticSearchMonitor.setValue(mInitializingShards, clusterHealth, "initializing_shards");
    ElasticSearchMonitor.setValue(mUnassignedShards, clusterHealth, "unassigned_shards");
    ElasticSearchMonitor.setValue(mDelayedUnassignedShards, clusterHealth, "delayed_unassigned_shards");
  }

  private Map<String, String> retrieveNodeHealth() {
    final Map<String, String> nodeIdToName = new HashMap<>();
    final JSONObject nodeHealth = get(ElasticSearchMonitor.ELASTIC_NODES);
    final JSONObject nodes = nodeHealth.optJSONObject("nodes");
    if (nodes != null) {
      for (final String nameKey : nodes.keySet()) {
        final JSONObject node = nodes.optJSONObject(nameKey);
        final String name = node.getString("name");
        final String nodeName = LibStr.isNotEmptyOrNull(name) ? name : "unknown-node";
        nodeIdToName.put(nameKey, nodeName);
        final JSONObject jvm = node.optJSONObject("jvm");
        if (jvm != null) {
          final JSONObject mem = jvm.optJSONObject("mem");
          ElasticSearchMonitor.addValue(mMemInitHeap, mem, "heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemInitHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxHeap, mem, "heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemInitNonHeap, mem, "non_heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemInitNonHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "non_heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxNonHeap, mem, "non_heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxNonHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "non_heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxDirect, mem, "direct_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxDirect, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "direct_max_in_bytes");
        }
      }
    }
    return nodeIdToName;
  }

  private void retrieveNodeStats() {
    final JSONObject nodeHealth = get(ElasticSearchMonitor.ELASTIC_NODES_STATS);
    final JSONObject nodes = nodeHealth.optJSONObject("nodes");
    if (nodes != null) {
      for (final String nameKey : nodes.keySet()) {
        final JSONObject node = nodes.optJSONObject(nameKey);
        final String name = node.getString("name");
        final String nodeName = LibStr.isNotEmptyOrNull(name) ? name : "unknown-node";
        final JSONObject process = node.optJSONObject("process");
        ElasticSearchMonitor.addValue(mFileDescLimitPerNode, process, "max_file_descriptors");
        ElasticSearchMonitor.addValue(mFileDescLimitPerNode, ElasticSearchMonitor.SPLIT_NODE, nodeName, process, "max_file_descriptors");
        final JSONObject indices = node.optJSONObject("indices");
        if (indices != null) {
          final JSONObject store = indices.optJSONObject("store");
          ElasticSearchMonitor.addValue(mStoreSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, store, "size_in_bytes");
          ElasticSearchMonitor.addValue(mStoreThrottleTime, ElasticSearchMonitor.SPLIT_NODE, nodeName, store, "throttle_time_in_millis");
          final JSONObject indexing = indices.optJSONObject("indexing");
          ElasticSearchMonitor.addValue(mIndexingThrottleTime, indexing, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mIndexingThrottleTime, ElasticSearchMonitor.SPLIT_NODE, nodeName, indexing, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mIndexingCurrent, indexing, "index_current");
          ElasticSearchMonitor.addValue(mIndexingCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, indexing, "index_current");
          ElasticSearchMonitor.addValue(mDeleteCurrent, indexing, "delete_current");
          ElasticSearchMonitor.addValue(mDeleteCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, indexing, "delete_current");
          final JSONObject search = indices.optJSONObject("search");
          ElasticSearchMonitor.addValue(mQueryCurrent, search, "query_current");
          ElasticSearchMonitor.addValue(mQueryCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, search, "query_current");
          ElasticSearchMonitor.addValue(mFetchCurrent, search, "fetch_current");
          ElasticSearchMonitor.addValue(mFetchCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, search, "fetch_current");
          ElasticSearchMonitor.addValue(mScrollCurrent, search, "scroll_current");
          ElasticSearchMonitor.addValue(mScrollCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, search, "scroll_current");
          final JSONObject queryCache = indices.optJSONObject("query_cache");
          ElasticSearchMonitor.addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, queryCache, "memory_size_in_bytes");
          final JSONObject fieldData = indices.optJSONObject("fielddata");
          ElasticSearchMonitor.addValue(mFiledDataSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, fieldData, "memory_size_in_bytes");
          final JSONObject percolate = indices.optJSONObject("percolate");
          ElasticSearchMonitor.addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, percolate, "memory_size_in_bytes");
          final JSONObject translog = indices.optJSONObject("translog");
          ElasticSearchMonitor.addValue(mTranslogSize, translog, "size_in_bytes");
          ElasticSearchMonitor.addValue(mTranslogSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, translog, "size_in_bytes");
          final JSONObject requestCache = indices.optJSONObject("request_cache");
          ElasticSearchMonitor.addValue(mRequestCacheSize, requestCache, "memory_size_in_bytes");
          ElasticSearchMonitor.addValue(mRequestCacheSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, requestCache, "memory_size_in_bytes");
          final JSONObject recovery = indices.optJSONObject("recovery");
          ElasticSearchMonitor.addValue(mRecoveryThrottleTime, recovery, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mRecoveryThrottleTime, ElasticSearchMonitor.SPLIT_NODE, nodeName, recovery, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mRecoveryAsSource, recovery, "current_as_source");
          ElasticSearchMonitor.addValue(mRecoveryAsSource, ElasticSearchMonitor.SPLIT_NODE, nodeName, recovery, "current_as_source");
          ElasticSearchMonitor.addValue(mRecoveryAsTarget, recovery, "current_as_target");
          ElasticSearchMonitor.addValue(mRecoveryAsTarget, ElasticSearchMonitor.SPLIT_NODE, nodeName, recovery, "current_as_target");
        }
      }
    }
  }

}
