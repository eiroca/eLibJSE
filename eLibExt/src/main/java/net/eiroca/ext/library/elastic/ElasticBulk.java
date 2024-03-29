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
package net.eiroca.ext.library.elastic;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.ext.library.http.WritableEntity;
import net.eiroca.library.system.Logs;

public class ElasticBulk {

  public static final String STR_BULKMIMETYPE = "application/json";

  public static final int ELASTIC_OVERLOAD = 429;

  private static final int DEFAULT_THREADS = 1;
  private static final int DEFAULT_BULKSIZE = 1 * 1024 * 1024;

  private static final Logger logger = Logs.getLogger();

  public ElasticBulkStats stats = new ElasticBulkStats();

  final private List<IndexEntry> data = new ArrayList<>();
  private final int version;
  private int size = 0;
  private boolean deflate = true;
  private final String encoding = "UTF-8";
  private String elasticServer;
  private int bulkSize = ElasticBulk.DEFAULT_BULKSIZE;
  private int numThreads = ElasticBulk.DEFAULT_THREADS;
  private boolean checkResult;
  private final ThreadPoolExecutor senderPool;
  private String authorization;

  private long discardTime = 1000;
  private long lastOverload;

  public ElasticBulk(final String server, final int version) {
    this(server, version, true, ElasticBulk.DEFAULT_BULKSIZE, ElasticBulk.DEFAULT_THREADS);
  }

  public ElasticBulk(final String elasticServer, final int version, final boolean checkResult, final int bulkSize, final int numThreads) {
    super();
    this.version = version;
    this.numThreads = numThreads;
    this.bulkSize = bulkSize;
    setElasticServer(elasticServer);
    this.checkResult = checkResult;
    senderPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(numThreads);
    open();
  }

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(final String authorization) {
    this.authorization = authorization;
  }

  public void open() {
    data.clear();
    size = 0;
  }

  public void add(final String index, final String type, final String id, final String pipeline, final String document) throws Exception {
    final long now = System.currentTimeMillis();
    final IndexEntry e = new IndexEntry(index, type, id, pipeline, document, version);
    size += e.entrySize();
    data.add(e);
    stats.incEventCount(1);
    stats.addAddTime(System.currentTimeMillis() - now);
    if (size > bulkSize) {
      ElasticBulk.logger.debug("Internal flush size:{}", size);
      flush();
    }
  }

  public void flush() throws Exception {
    if (data.size() < 1) { return; }
    final long flushStartTime = System.currentTimeMillis();
    final WritableEntity entity = new WritableEntity(ElasticBulk.STR_BULKMIMETYPE, encoding, deflate);
    final OutputStream os = entity.openBuffer();
    final int events = data.size();
    for (final IndexEntry e : data) {
      final String indexReq = e.bulkEntry();
      ElasticBulk.logger.debug(indexReq);
      os.write(indexReq.getBytes(encoding));
    }
    entity.closeBuffer();
    final ElasticSenderThread sender = new ElasticSenderThread(this, entity, events);
    stats.addFlushTime(System.currentTimeMillis() - flushStartTime);
    if (numThreads > 1) {
      ElasticBulk.logger.debug("Elastic Index taskCount:" + senderPool.getTaskCount() + " queueSize: " + senderPool.getQueue().size() + " activeCount:" + senderPool.getActiveCount());
      senderPool.execute(sender);
    }
    else {
      sender.run();
    }
    data.clear();
    size = 0;
  }

  public void close() throws Exception {
    ElasticBulk.logger.debug("Closing");
    flush();
  }

  public void checkResult(final int responseCode, final HttpEntity entity, final int size) {
    if (checkResult) {
      int errors = size;
      final String resultStr = HttpClientHelper.consume(entity);
      if ((responseCode < 400) && (resultStr != null)) {
        errors = checkResult(null, resultStr);
      }
      stats.incEventErrors(errors);
    }
  }

  public int checkResult(final List<IndexEntry> data, final String resultStr) {
    final JsonElement result = JsonParser.parseString(resultStr);
    JsonArray items = null;
    items = result.getAsJsonObject().get("items").getAsJsonArray();
    int idx = 0;
    int errors = 0;
    if ((data != null) && (data.size() != items.size())) {
      ElasticBulk.logger.warn("Elastic Error: INPUT and OUTPUT json element size differs!");
      errors = items.size();
    }
    else {
      if (items.size() > 0) {
        boolean overload = false;
        for (idx = 0; idx < items.size(); idx++) {
          final JsonElement e = items.get(idx);
          final JsonObject o = (e != null) ? e.getAsJsonObject() : null;
          final JsonObject index = (o != null) ? o.getAsJsonObject("index") : null;
          final JsonElement el = (index != null) ? index.get("status") : null;
          final int status = (el != null) ? el.getAsInt() : 999;
          final IndexEntry entry = (data != null) ? data.get(idx) : null;
          if (status >= 400) {
            errors++;
            if (status == ElasticBulk.ELASTIC_OVERLOAD) {
              overload = true;
            }
            else {
              if (entry != null) {
                ElasticBulk.logger.warn("Elastic Error:\n" + entry.meta + "\n" + entry.data);
              }
              else {
                ElasticBulk.logger.warn("Elastic Response: " + e);
              }
            }
          }
          else if (entry != null) {
            final JsonElement id = (index != null) ? index.get("_id") : null;
            entry._id = (id != null) ? id.getAsString() : null;
          }
        }
        setOverload(overload);
      }
    }
    return errors;
  }

  public boolean isDeflate() {
    return deflate;
  }

  public void setDeflate(final boolean deflate) {
    this.deflate = deflate;
  }

  public int getNumThreads() {
    return numThreads;
  }

  public void setNumThreads(final int numThreads) {
    this.numThreads = numThreads;
    senderPool.setMaximumPoolSize(numThreads);
    senderPool.setCorePoolSize(numThreads);
  }

  public int getBulkSize() {
    return bulkSize;
  }

  public void setBulkSize(final int bulkSize) {
    this.bulkSize = bulkSize;
  }

  public boolean isCheckResult() {
    return checkResult;
  }

  public void setCheckResult(final boolean checkResult) {
    this.checkResult = checkResult;
  }

  public int getQueueSize() {
    return numThreads > 1 ? senderPool.getQueue().size() : 0;
  }

  public void setOverload(final boolean overload) {
    if (overload) {
      final long now = System.currentTimeMillis();
      if (now > lastOverload) {
        lastOverload = now + discardTime;
        ElasticBulk.logger.info("Elastic overload {} until {}", now, lastOverload);
      }
    }
  }

  public boolean isOverload() {
    final long now = System.currentTimeMillis();
    return (now <= lastOverload);
  }

  public void setDiscarTime(long discardTime) {
    if (discardTime < 0) {
      discardTime = 0;
    }
    this.discardTime = discardTime;
  }

  public long getDiscardTime() {
    return discardTime;
  }

  public long getLastOverload() {
    return lastOverload > 0 ? lastOverload - discardTime : 0;
  }

  public String getElasticServer() {
    return elasticServer;
  }

  public void setElasticServer(final String elasticServer) {
    this.elasticServer = elasticServer;
  }

}
