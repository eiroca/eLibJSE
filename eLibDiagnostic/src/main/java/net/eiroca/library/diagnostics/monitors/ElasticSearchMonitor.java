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
import net.eiroca.library.metrics.MeasureGroup;
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

  MeasureGroup mgElasticSearch = new MeasureGroup("ElasticSearch Statistics", "ElasticSearch - {0}");

  Measure mActivePrimaryShards = new Measure(mgElasticSearch, "ActivePrimaryShards");
  Measure mActiveShards = new Measure(mgElasticSearch, "ActiveShards");
  Measure mActiveShardsPercent = new Measure(mgElasticSearch, "ActiveShardsPercent");
  Measure mCompletionSize = new Measure(mgElasticSearch, "CompletionSize");
  Measure mDataNodeCount = new Measure(mgElasticSearch, "DataNodeCount");
  Measure mDelayedUnassignedShards = new Measure(mgElasticSearch, "DelayedUnassignedShards");
  Measure mDeleteCurrent = new Measure(mgElasticSearch, "DeleteCurrent");
  Measure mDeletedCount = new Measure(mgElasticSearch, "DeletedCount");
  RateMeasure mDocumentDeletedRate = new RateMeasure(mgElasticSearch, "DeletedCountPerSecond", mDeletedCount, TimeUnit.SECONDS, 0.0);
  Measure mDocumentCount = new Measure(mgElasticSearch, "DocCount");
  RateMeasure mDocumentRate = new RateMeasure(mgElasticSearch, "DocCountPerSecond", mDocumentCount, TimeUnit.SECONDS, 0.0);
  Measure mFetchCurrent = new Measure(mgElasticSearch, "FetchCurrent");
  Measure mFieldDataEvictions = new Measure(mgElasticSearch, "FieldDataEvictions");
  Measure mFiledDataSize = new Measure(mgElasticSearch, "FieldDataSize");
  Measure mFileDescLimitPerNode = new Measure(mgElasticSearch, "FileDescriptorLimit");
  Measure mFileDescriptorCount = new Measure(mgElasticSearch, "FileDescriptorCount");
  Measure mFileSystemSize = new Measure(mgElasticSearch, "FileSystemSize");
  Measure mIndexCount = new Measure(mgElasticSearch, "IndexCount");
  Measure mIndexingCurrent = new Measure(mgElasticSearch, "IndexingCurrent");
  Measure mIndexingThrottleTime = new Measure(mgElasticSearch, "IndexingThrottleTime");
  Measure mInitializingShards = new Measure(mgElasticSearch, "InitializingShards");
  Measure mMemInitHeap = new Measure(mgElasticSearch, "InitHeap");
  Measure mMemInitNonHeap = new Measure(mgElasticSearch, "InitNonHeap");
  Measure mMemMaxDirect = new Measure(mgElasticSearch, "MaxDirect");
  Measure mMemMaxHeap = new Measure(mgElasticSearch, "MaxHeap");
  Measure mMemMaxNonHeap = new Measure(mgElasticSearch, "MaxNonHeap");
  Measure mNodeCount = new Measure(mgElasticSearch, "NodeCount");
  Measure mPercolateCount = new Measure(mgElasticSearch, "PercolateCount");
  Measure mPercolateSize = new Measure(mgElasticSearch, "PercolateSize");
  Measure mQueryCacheSize = new Measure(mgElasticSearch, "QueryCacheSize");
  Measure mQueryCurrent = new Measure(mgElasticSearch, "QueryCurrent");
  Measure mRecoveryAsSource = new Measure(mgElasticSearch, "RecoveryAsSource");
  Measure mRecoveryAsTarget = new Measure(mgElasticSearch, "RecoveryAsTarget");
  Measure mRecoveryThrottleTime = new Measure(mgElasticSearch, "RecoveryThrottleTime");
  Measure mRelocatingShards = new Measure(mgElasticSearch, "RelocatingShards");
  Measure mRequestCacheSize = new Measure(mgElasticSearch, "RequestCacheSize");
  Measure mScrollCurrent = new Measure(mgElasticSearch, "ScrollCurrent");
  Measure mSegmentCount = new Measure(mgElasticSearch, "SegmentCount");
  Measure mSegmentSize = new Measure(mgElasticSearch, "SegmentSize");
  Measure mShardCount = new Measure(mgElasticSearch, "ShardCount");
  Measure mStoreSize = new Measure(mgElasticSearch, "StoreSize");
  Measure mStoreThrottleTime = new Measure(mgElasticSearch, "StoreThrottleTime");
  Measure mTranslogSize = new Measure(mgElasticSearch, "TranslogSize");
  Measure mUnassignedShards = new Measure(mgElasticSearch, "UnassignedShards");

  String baseURL;

  @Override
  public void loadMetricGroup(final List<MeasureGroup> groups) {
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
      measure.getSplitting(split).addValue(splitName, node.getDouble(key));
    }
  }

  private static void addValue(final Measure measure, final String split, final JSONObject node, final String key) {
    if ((node != null) && (node.has(key))) {
      measure.getSplitting(split).addValue(key, node.getDouble(key));
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
    setValue(mIndexCount, index, "count");
    final JSONObject shards = index.optJSONObject("shards");
    setValue(mShardCount, shards, "total");
    addValue(mShardCount, ElasticSearchMonitor.SPLIT_STATE, "primary", shards, "primaries");
    addValue(mShardCount, ElasticSearchMonitor.SPLIT_STATE, "replicationFactor", shards, "replication");
    final JSONObject docs = index.optJSONObject("docs");
    setValue(mDocumentCount, docs, "count");
    setValue(mDeletedCount, docs, "deleted");
    final JSONObject store = index.optJSONObject("store");
    setValue(mStoreThrottleTime, store, "throttle_time_in_millis");
    setValue(mStoreSize, store, "size_in_bytes");
    final JSONObject fielddata = index.optJSONObject("fielddata");
    setValue(mFiledDataSize, fielddata, "memory_size_in_bytes");
    setValue(mFieldDataEvictions, fielddata, "evictions");
    final JSONObject query_cache = index.optJSONObject("query_cache");
    setValue(mQueryCacheSize, query_cache, "memory_size_in_bytes");
    addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "total_count");
    addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "hit_count");
    addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "miss_count");
    addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "cache_size");
    addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "cache_count");
    addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_STAT, query_cache, "evictions");
    final JSONObject completion = index.optJSONObject("completion");
    setValue(mCompletionSize, completion, "size_in_bytes");
    final JSONObject segments = index.optJSONObject("segments");
    setValue(mSegmentCount, segments, "count");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "count");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "terms_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "stored_fields_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "term_vectors_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "norms_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "doc_values_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "index_writer_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "index_writer_max_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "version_map_memory_in_bytes");
    addValue(mSegmentSize, ElasticSearchMonitor.SPLIT_STAT, segments, "fixed_bit_set_memory_in_bytes");
    final JSONObject percolate = index.optJSONObject("percolate");
    setValue(mPercolateCount, percolate, "current");
    addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "total");
    addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "time_in_millis");
    addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "current");
    addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "memory_size_in_bytes");
    addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_STAT, percolate, "queries");
    final JSONObject nodes = clusterStats.optJSONObject("nodes");
    if (nodes != null) {
      final JSONObject process = nodes.optJSONObject("process");
      if (process != null) {
        final JSONObject fileDesc = process.optJSONObject("open_file_descriptors");
        setValue(mFileDescriptorCount, fileDesc, "max");
        addValue(mFileDescriptorCount, ElasticSearchMonitor.SPLIT_STAT, fileDesc, "min");
        addValue(mFileDescriptorCount, ElasticSearchMonitor.SPLIT_STAT, fileDesc, "max");
        addValue(mFileDescriptorCount, ElasticSearchMonitor.SPLIT_STAT, fileDesc, "avg");
      }
      final JSONObject fs = nodes.optJSONObject("fs");
      addValue(mFileSystemSize, fs, "free_in_bytes");
      addValue(mFileSystemSize, ElasticSearchMonitor.SPLIT_STAT, fs, "total_in_bytes");
      addValue(mFileSystemSize, ElasticSearchMonitor.SPLIT_STAT, fs, "free_in_bytes");
      addValue(mFileSystemSize, ElasticSearchMonitor.SPLIT_STAT, fs, "available_in_bytes");
    }
  }

  private void retrieveClusterHealth() {
    final JSONObject clusterHealth = get(ElasticSearchMonitor.ELASTIC_CLUSTER_HEALTH);
    setValue(mNodeCount, clusterHealth, "number_of_nodes");
    setValue(mDataNodeCount, clusterHealth, "number_of_data_nodes");
    setValue(mActivePrimaryShards, clusterHealth, "active_primary_shards");
    setValue(mActiveShardsPercent, clusterHealth, "active_shards_percent_as_number");
    setValue(mActiveShards, clusterHealth, "active_shards");
    setValue(mRelocatingShards, clusterHealth, "relocating_shards");
    setValue(mInitializingShards, clusterHealth, "initializing_shards");
    setValue(mUnassignedShards, clusterHealth, "unassigned_shards");
    setValue(mDelayedUnassignedShards, clusterHealth, "delayed_unassigned_shards");
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
          addValue(mMemInitHeap, mem, "heap_init_in_bytes");
          addValue(mMemInitHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "heap_init_in_bytes");
          addValue(mMemMaxHeap, mem, "heap_max_in_bytes");
          addValue(mMemMaxHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "heap_max_in_bytes");
          addValue(mMemInitNonHeap, mem, "non_heap_init_in_bytes");
          addValue(mMemInitNonHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "non_heap_init_in_bytes");
          addValue(mMemMaxNonHeap, mem, "non_heap_max_in_bytes");
          addValue(mMemMaxNonHeap, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "non_heap_max_in_bytes");
          addValue(mMemMaxDirect, mem, "direct_max_in_bytes");
          addValue(mMemMaxDirect, ElasticSearchMonitor.SPLIT_NODE, nodeName, mem, "direct_max_in_bytes");
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
        addValue(mFileDescLimitPerNode, process, "max_file_descriptors");
        addValue(mFileDescLimitPerNode, ElasticSearchMonitor.SPLIT_NODE, nodeName, process, "max_file_descriptors");
        final JSONObject indices = node.optJSONObject("indices");
        if (indices != null) {
          final JSONObject store = indices.optJSONObject("store");
          addValue(mStoreSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, store, "size_in_bytes");
          addValue(mStoreThrottleTime, ElasticSearchMonitor.SPLIT_NODE, nodeName, store, "throttle_time_in_millis");
          final JSONObject indexing = indices.optJSONObject("indexing");
          addValue(mIndexingThrottleTime, indexing, "throttle_time_in_millis");
          addValue(mIndexingThrottleTime, ElasticSearchMonitor.SPLIT_NODE, nodeName, indexing, "throttle_time_in_millis");
          addValue(mIndexingCurrent, indexing, "index_current");
          addValue(mIndexingCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, indexing, "index_current");
          addValue(mDeleteCurrent, indexing, "delete_current");
          addValue(mDeleteCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, indexing, "delete_current");
          final JSONObject search = indices.optJSONObject("search");
          addValue(mQueryCurrent, search, "query_current");
          addValue(mQueryCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, search, "query_current");
          addValue(mFetchCurrent, search, "fetch_current");
          addValue(mFetchCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, search, "fetch_current");
          addValue(mScrollCurrent, search, "scroll_current");
          addValue(mScrollCurrent, ElasticSearchMonitor.SPLIT_NODE, nodeName, search, "scroll_current");
          final JSONObject queryCache = indices.optJSONObject("query_cache");
          addValue(mQueryCacheSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, queryCache, "memory_size_in_bytes");
          final JSONObject fieldData = indices.optJSONObject("fielddata");
          addValue(mFiledDataSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, fieldData, "memory_size_in_bytes");
          final JSONObject percolate = indices.optJSONObject("percolate");
          addValue(mPercolateSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, percolate, "memory_size_in_bytes");
          final JSONObject translog = indices.optJSONObject("translog");
          addValue(mTranslogSize, translog, "size_in_bytes");
          addValue(mTranslogSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, translog, "size_in_bytes");
          final JSONObject requestCache = indices.optJSONObject("request_cache");
          addValue(mRequestCacheSize, requestCache, "memory_size_in_bytes");
          addValue(mRequestCacheSize, ElasticSearchMonitor.SPLIT_NODE, nodeName, requestCache, "memory_size_in_bytes");
          final JSONObject recovery = indices.optJSONObject("recovery");
          addValue(mRecoveryThrottleTime, recovery, "throttle_time_in_millis");
          addValue(mRecoveryThrottleTime, ElasticSearchMonitor.SPLIT_NODE, nodeName, recovery, "throttle_time_in_millis");
          addValue(mRecoveryAsSource, recovery, "current_as_source");
          addValue(mRecoveryAsSource, ElasticSearchMonitor.SPLIT_NODE, nodeName, recovery, "current_as_source");
          addValue(mRecoveryAsTarget, recovery, "current_as_target");
          addValue(mRecoveryAsTarget, ElasticSearchMonitor.SPLIT_NODE, nodeName, recovery, "current_as_target");
        }
      }
    }
  }

}
