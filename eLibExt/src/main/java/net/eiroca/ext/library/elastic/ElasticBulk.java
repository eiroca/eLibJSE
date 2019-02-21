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
package net.eiroca.ext.library.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.library.system.Logs;

public class ElasticBulk {

  private static final Logger logger = Logs.getLogger();

  private static final int DEFAULT_THREADS = 1;
  private static final int DEFAULT_BULKSIZE = 1 * 1024 * 1024;

  public ElasticBulkStats stats = new ElasticBulkStats();

  final private List<IndexEntry> data = new ArrayList<>();
  private int size = 0;
  String encoding = "UTF-8";
  String elasticServer;
  int bulkSize = ElasticBulk.DEFAULT_BULKSIZE;
  int numThreads = ElasticBulk.DEFAULT_THREADS;
  boolean checkResult;
  private final ThreadPoolExecutor senderPool;

  public ElasticBulk(final String server) {
    this(server, true, ElasticBulk.DEFAULT_BULKSIZE, ElasticBulk.DEFAULT_THREADS);
  }

  public ElasticBulk(final String elasticServer, final boolean checkResult, final int bulkSize, final int numThreads) {
    super();
    this.numThreads = numThreads;
    this.bulkSize = bulkSize;
    this.elasticServer = elasticServer;
    this.checkResult = checkResult;
    senderPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(numThreads);
    open();
  }

  public void open() {
    data.clear();
    size = 0;
  }

  public void add(final String index, final String type, final String id, final String pipeline, final String document) throws Exception {
    final long now = System.currentTimeMillis();
    final IndexEntry e = new IndexEntry(index, type, id, pipeline, document);
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
    final StringBuilder body = new StringBuilder();
    final int events = data.size();
    for (final IndexEntry e : data) {
      final String indexReq = e.bulkEntry();
      ElasticBulk.logger.debug(indexReq);
      body.append(indexReq);
    }
    final HttpEntity entity = new StringEntity(body.toString(), encoding);
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
      if (responseCode < 400) {
        final String resultStr = HttpClientHelper.consume(entity);
        if (resultStr != null) {
          errors = ElasticBulk.checkResult(null, resultStr);
        }
      }
      stats.incEventErrors(errors);
    }
  }

  public static int checkResult(final List<IndexEntry> data, final String resultStr) {
    final JsonParser parser = new JsonParser();
    final JsonElement result = parser.parse(resultStr);
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
        for (idx = 0; idx < items.size(); idx++) {
          final JsonElement e = items.get(idx);
          final JsonObject o = (e != null) ? e.getAsJsonObject() : null;
          final JsonObject index = (o != null) ? o.getAsJsonObject("index") : null;
          final JsonElement el = (index != null) ? index.get("status") : null;
          final int status = (el != null) ? el.getAsInt() : 999;
          final IndexEntry entry = (data != null) ? data.get(idx) : null;
          if (status >= 400) {
            errors++;
            if (entry != null) {
              ElasticBulk.logger.warn("Elastic Error:\n" + entry.meta + "\n" + entry.data);
            }
            else {
              ElasticBulk.logger.warn("Elastic Response: " + e);
            }
          }
          else if (entry != null) {
            final JsonElement id = (index != null) ? index.get("_id") : null;
            entry._id = (id != null) ? id.getAsString() : null;
          }
        }
      }
    }
    return errors;
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

}
