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

  protected static final String CONFIG_ELASTICURL = "elasticURL";

  MeasureGroup mgElasticSearch = new MeasureGroup("ElasticSearch Monitor", "ElasticSearch - {0}");

  Measure mNodeCount = new Measure(mgElasticSearch, "NodeCount");
  Measure mIndexCount = new Measure(mgElasticSearch, "IndexCount");
  Measure mShardCount = new Measure(mgElasticSearch, "ShardCount");
  Measure mDocumentCount = new Measure(mgElasticSearch, "DocCount");
  Measure mDocumentDeletedCount = new Measure(mgElasticSearch, "DeletedCount");
  RateMeasure mDocumentRate = new RateMeasure(mgElasticSearch, "DocCountPerSecond", mDocumentCount, TimeUnit.SECONDS, 0.0);
  RateMeasure mDocumentDeletedRate = new RateMeasure(mgElasticSearch, "DeletedCountPerSecond", mDocumentDeletedCount, TimeUnit.SECONDS, 0.0);
  Measure mDataNodeCount = new Measure(mgElasticSearch, "DataNodeCount");
  Measure mActivePrimaryShards = new Measure(mgElasticSearch, "ActivePrimaryShards");
  Measure mActiveShardsPercent = new Measure(mgElasticSearch, "ActiveShardsPercent");
  Measure mActiveShards = new Measure(mgElasticSearch, "ActiveShards");
  Measure mRelocatingShards = new Measure(mgElasticSearch, "RelocatingShards");
  Measure mInitializingShards = new Measure(mgElasticSearch, "InitializingShards");
  Measure mUnassignedShards = new Measure(mgElasticSearch, "UnassignedShards");
  Measure mDelayedUnassignedShards = new Measure(mgElasticSearch, "DelayedUnassignedShards");
  Measure mMemInitHeap = new Measure(mgElasticSearch, "InitHeap");
  Measure mMemMaxHeap = new Measure(mgElasticSearch, "MaxHeap");
  Measure mMemInitNonHeap = new Measure(mgElasticSearch, "InitNonHeap");
  Measure mMemMaxNonHeap = new Measure(mgElasticSearch, "MaxNonHeap");
  Measure mMemMaxDirect = new Measure(mgElasticSearch, "MaxDirect");
  Measure mStoreSize = new Measure(mgElasticSearch, "StoreSize");
  Measure mStoreThrottleTime = new Measure(mgElasticSearch, "StoreThrottleTime");
  Measure mIndexingThrottleTime = new Measure(mgElasticSearch, "IndexingThrottleTime");
  Measure mIndexingCurrent = new Measure(mgElasticSearch, "IndexingCurrent");
  Measure mDeleteCurrent = new Measure(mgElasticSearch, "DeleteCurrent");
  Measure mQueryCurrent = new Measure(mgElasticSearch, "QueryCurrent");
  Measure mFetchCurrent = new Measure(mgElasticSearch, "FetchCurrent");
  Measure mScrollCurrent = new Measure(mgElasticSearch, "ScrollCurrent");
  Measure mQueryCacheSize = new Measure(mgElasticSearch, "QueryCacheSize");
  Measure mFiledDataSize = new Measure(mgElasticSearch, "FieldDataSize");
  Measure mFieldDataEvictions = new Measure(mgElasticSearch, "FieldDataEvictions");
  Measure mPercolateSize = new Measure(mgElasticSearch, "PercolateSize");
  Measure mTranslogSize = new Measure(mgElasticSearch, "TranslogSize");
  Measure mRequestCacheSize = new Measure(mgElasticSearch, "RequestCacheSize");
  Measure mRecoveryThrottleTime = new Measure(mgElasticSearch, "RecoveryThrottleTime");
  Measure mRecoveryAsSource = new Measure(mgElasticSearch, "RecoveryAsSource");
  Measure mRecoveryAsTarget = new Measure(mgElasticSearch, "RecoveryAsTarget");
  Measure mCompletionSize = new Measure(mgElasticSearch, "CompletionSize");
  Measure mSegmentCount = new Measure(mgElasticSearch, "SegmentCount");
  Measure mSegmentSize = new Measure(mgElasticSearch, "SegmentSize");
  Measure mFileDescriptorCount = new Measure(mgElasticSearch, "FileDescriptorCount");
  Measure mFileDescLimitPerNode = new Measure(mgElasticSearch, "FileDescriptorLimit");
  Measure mFileSystemSize = new Measure(mgElasticSearch, "FileSystemSize");

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
    return ok;
  }

  @Override
  public ReturnObject fetchResponse() throws CommandException {
    retrieveClusterState();
    retrieveClusterHealth();
    retrieveNodeHealth();
    retrieveNodeStats();
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
    return new JSONObject(jsonStr);
  }

  private void retrieveClusterState() {
    final JSONObject clusterStats = get("/_cluster/stats");
    final JSONObject _nodes = clusterStats.getJSONObject("_nodes");
    ElasticSearchMonitor.setValue(mNodeCount, _nodes, "count");
    final JSONObject index = clusterStats.getJSONObject("indices");
    ElasticSearchMonitor.setValue(mIndexCount, index, "count");
    final JSONObject shards = index.getJSONObject("shards");
    ElasticSearchMonitor.setValue(mShardCount, shards, "total");
    ElasticSearchMonitor.addValue(mShardCount, "State", "primary", shards, "primaries");
    ElasticSearchMonitor.addValue(mShardCount, "State", "replicationFactor", shards, "replication");
    final JSONObject docs = index.getJSONObject("docs");
    ElasticSearchMonitor.setValue(mDocumentCount, docs, "count");
    ElasticSearchMonitor.setValue(mDocumentDeletedCount, docs, "deleted");
    final JSONObject store = index.getJSONObject("store");
    ElasticSearchMonitor.setValue(mStoreThrottleTime, store, "throttle_time_in_millis");
    ElasticSearchMonitor.setValue(mStoreSize, store, "size_in_bytes");
    final JSONObject fielddata = index.getJSONObject("fielddata");
    ElasticSearchMonitor.setValue(mFiledDataSize, fielddata, "memory_size_in_bytes");
    ElasticSearchMonitor.setValue(mFieldDataEvictions, fielddata, "evictions");
    final JSONObject query_cache = index.getJSONObject("query_cache");
    ElasticSearchMonitor.setValue(mQueryCacheSize, query_cache, "memory_size_in_bytes");
    ElasticSearchMonitor.addValue(mQueryCacheSize, "Stat", query_cache, "total_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, "Stat", query_cache, "hit_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, "Stat", query_cache, "miss_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, "Stat", query_cache, "cache_size");
    ElasticSearchMonitor.addValue(mQueryCacheSize, "Stat", query_cache, "cache_count");
    ElasticSearchMonitor.addValue(mQueryCacheSize, "Stat", query_cache, "evictions");
    final JSONObject completion = index.getJSONObject("completion");
    ElasticSearchMonitor.setValue(mCompletionSize, completion, "size_in_bytes");
    final JSONObject segments = index.getJSONObject("segments");
    ElasticSearchMonitor.setValue(mSegmentCount, segments, "count");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "count");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "terms_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "stored_fields_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "term_vectors_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "norms_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "doc_values_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "index_writer_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "index_writer_max_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "version_map_memory_in_bytes");
    ElasticSearchMonitor.addValue(mSegmentSize, "Stat", segments, "fixed_bit_set_memory_in_bytes");
    final JSONObject percolate = index.getJSONObject("percolate");
    ElasticSearchMonitor.setValue(mPercolateSize, percolate, "current");
    ElasticSearchMonitor.addValue(mPercolateSize, "Stat", percolate, "total");
    ElasticSearchMonitor.addValue(mPercolateSize, "Stat", percolate, "time_in_millis");
    ElasticSearchMonitor.addValue(mPercolateSize, "Stat", percolate, "current");
    ElasticSearchMonitor.addValue(mPercolateSize, "Stat", percolate, "memory_size_in_bytes");
    ElasticSearchMonitor.addValue(mPercolateSize, "Stat", percolate, "queries");
    final JSONObject nodes = clusterStats.getJSONObject("nodes");
    if (nodes != null) {
      final JSONObject process = nodes.getJSONObject("process");
      if (process != null) {
        final JSONObject fileDesc = process.getJSONObject("open_file_descriptors");
        ElasticSearchMonitor.setValue(mFileDescriptorCount, fileDesc, "max");
        ElasticSearchMonitor.addValue(mFileDescriptorCount, "Stat", fileDesc, "min");
        ElasticSearchMonitor.addValue(mFileDescriptorCount, "Stat", fileDesc, "max");
        ElasticSearchMonitor.addValue(mFileDescriptorCount, "Stat", fileDesc, "avg");
      }
      final JSONObject fs = nodes.getJSONObject("fs");
      ElasticSearchMonitor.addValue(mFileSystemSize, fs, "free_in_bytes");
      ElasticSearchMonitor.addValue(mFileSystemSize, "Stat", fs, "total_in_bytes");
      ElasticSearchMonitor.addValue(mFileSystemSize, "Stat", fs, "free_in_bytes");
      ElasticSearchMonitor.addValue(mFileSystemSize, "Stat", fs, "available_in_bytes");
    }
  }

  private void retrieveClusterHealth() {
    final JSONObject clusterHealth = get("/_cluster/health");
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
    final JSONObject nodeHealth = get("/_nodes");
    final JSONObject nodes = nodeHealth.getJSONObject("nodes");
    if (nodes != null) {
      for (final String nameKey : nodes.keySet()) {
        final JSONObject node = nodes.getJSONObject(nameKey);
        final String name = node.getString("name");
        final String nodeName = LibStr.isNotEmptyOrNull(name) ? name : "unknown-node";
        nodeIdToName.put(nameKey, nodeName);
        final JSONObject jvm = node.getJSONObject("jvm");
        if (jvm != null) {
          final JSONObject mem = jvm.getJSONObject("mem");
          ElasticSearchMonitor.addValue(mMemInitHeap, mem, "heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemInitHeap, "Node", nodeName, mem, "heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxHeap, mem, "heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxHeap, "Node", nodeName, mem, "heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemInitNonHeap, mem, "non_heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemInitNonHeap, "Node", nodeName, mem, "non_heap_init_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxNonHeap, mem, "non_heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxNonHeap, "Node", nodeName, mem, "non_heap_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxDirect, mem, "direct_max_in_bytes");
          ElasticSearchMonitor.addValue(mMemMaxDirect, "Node", nodeName, mem, "direct_max_in_bytes");
        }
      }
    }
    return nodeIdToName;
  }

  private void retrieveNodeStats() {
    final JSONObject nodeHealth = get("/_nodes/stats");
    final JSONObject nodes = nodeHealth.getJSONObject("nodes");
    if (nodes != null) {
      for (final String nameKey : nodes.keySet()) {
        final JSONObject node = nodes.getJSONObject(nameKey);
        final String name = node.getString("name");
        final String nodeName = LibStr.isNotEmptyOrNull(name) ? name : "unknown-node";
        final JSONObject process = node.getJSONObject("process");
        ElasticSearchMonitor.addValue(mFileDescLimitPerNode, process, "max_file_descriptors");
        ElasticSearchMonitor.addValue(mFileDescLimitPerNode, "Node", nodeName, process, "max_file_descriptors");
        final JSONObject indices = node.getJSONObject("indices");
        if (indices != null) {
          final JSONObject store = indices.getJSONObject("store");
          ElasticSearchMonitor.addValue(mStoreSize, "Node", nodeName, store, "size_in_bytes");
          ElasticSearchMonitor.addValue(mStoreThrottleTime, "Node", nodeName, store, "throttle_time_in_millis");
          final JSONObject indexing = indices.getJSONObject("indexing");
          ElasticSearchMonitor.addValue(mIndexingThrottleTime, indexing, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mIndexingThrottleTime, "Node", nodeName, indexing, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mIndexingCurrent, indexing, "index_current");
          ElasticSearchMonitor.addValue(mIndexingCurrent, "Node", nodeName, indexing, "index_current");
          ElasticSearchMonitor.addValue(mDeleteCurrent, indexing, "delete_current");
          ElasticSearchMonitor.addValue(mDeleteCurrent, "Node", nodeName, indexing, "delete_current");
          final JSONObject search = indices.getJSONObject("search");
          ElasticSearchMonitor.addValue(mQueryCurrent, search, "query_current");
          ElasticSearchMonitor.addValue(mQueryCurrent, "Node", nodeName, search, "query_current");
          ElasticSearchMonitor.addValue(mFetchCurrent, search, "fetch_current");
          ElasticSearchMonitor.addValue(mFetchCurrent, "Node", nodeName, search, "fetch_current");
          ElasticSearchMonitor.addValue(mScrollCurrent, search, "scroll_current");
          ElasticSearchMonitor.addValue(mScrollCurrent, "Node", nodeName, search, "scroll_current");
          final JSONObject queryCache = indices.getJSONObject("query_cache");
          ElasticSearchMonitor.addValue(mQueryCacheSize, "Node", nodeName, queryCache, "memory_size_in_bytes");
          final JSONObject fieldData = indices.getJSONObject("fielddata");
          ElasticSearchMonitor.addValue(mFiledDataSize, "Node", nodeName, fieldData, "memory_size_in_bytes");
          final JSONObject percolate = indices.getJSONObject("percolate");
          ElasticSearchMonitor.addValue(mPercolateSize, "Node", nodeName, percolate, "memory_size_in_bytes");
          final JSONObject translog = indices.getJSONObject("translog");
          ElasticSearchMonitor.addValue(mTranslogSize, translog, "size_in_bytes");
          ElasticSearchMonitor.addValue(mTranslogSize, "Node", nodeName, translog, "size_in_bytes");
          final JSONObject requestCache = indices.getJSONObject("request_cache");
          ElasticSearchMonitor.addValue(mRequestCacheSize, requestCache, "memory_size_in_bytes");
          ElasticSearchMonitor.addValue(mRequestCacheSize, "Node", nodeName, requestCache, "memory_size_in_bytes");
          final JSONObject recovery = indices.getJSONObject("recovery");
          ElasticSearchMonitor.addValue(mRecoveryThrottleTime, recovery, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mRecoveryThrottleTime, "Node", nodeName, recovery, "throttle_time_in_millis");
          ElasticSearchMonitor.addValue(mRecoveryAsSource, recovery, "current_as_source");
          ElasticSearchMonitor.addValue(mRecoveryAsSource, "Node", nodeName, recovery, "current_as_source");
          ElasticSearchMonitor.addValue(mRecoveryAsTarget, recovery, "current_as_target");
          ElasticSearchMonitor.addValue(mRecoveryAsTarget, "Node", nodeName, recovery, "current_as_target");
        }
      }
    }
  }

}
