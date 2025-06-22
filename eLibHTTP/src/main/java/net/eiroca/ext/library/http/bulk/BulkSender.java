/**
 *
 * Copyright (C) 1999-2025 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.ext.library.http.bulk;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import net.eiroca.ext.library.http.WritableEntity;
import net.eiroca.library.system.Logs;

public abstract class BulkSender<T extends BulkEntry> {

  private static final Logger logger = Logs.getLogger();

  public final BulkStats stats = new BulkStats();

  protected final int version;
  protected final List<BulkEntry> data = new ArrayList<>();
  protected final ThreadPoolExecutor senderPool;

  protected int size = 0;

  private String authorization;
  private String serverURL;
  private boolean deflate = true;
  private String encoding = "UTF-8";
  private int bulkSize;
  private int numThreads;
  private String contentType = "text/plain";
  private String acceptType = "text/plain";
  private byte[] header = null;
  private byte[] footer = null;
  private byte[] separator = null;

  public BulkSender(final String server, final int version, final int bulkSize, final int numThreads) {
    super();
    this.version = version;
    this.numThreads = numThreads;
    this.bulkSize = bulkSize;
    setServerURL(server);
    senderPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(numThreads);
    open();
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public byte[] getHeader() {
    return header;
  }

  public void setHeader(byte[] header) {
    this.header = header;
  }

  public byte[] getFooter() {
    return footer;
  }

  public void setFooter(byte[] footer) {
    this.footer = footer;
  }

  public byte[] getSeparator() {
    return separator;
  }

  public void setSeparator(byte[] separator) {
    this.separator = separator;
  }

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(final String authorization) {
    this.authorization = authorization;
  }

  public String getServerURL() {
    return serverURL;
  }

  public void setServerURL(final String server) {
    serverURL = server;
  }

  public boolean checkOverload(final int responseCode) {
    return false;
  }

  public boolean checkFailed(final int responseCode) {
    return (responseCode >= 300);
  }

  public boolean checkValidResponse(final int responseCode, final HttpEntity entity, final int numEvents) {
    return true;
  }

  public boolean isOverload() {
    return false;
  }

  public void setOverload(final boolean overload) {
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getAcceptType() {
    return acceptType;
  }

  public void setAcceptType(String acceptType) {
    this.acceptType = acceptType;
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

  public int getQueueSize() {
    return numThreads > 1 ? senderPool.getQueue().size() : 0;
  }

  public void open() {
    data.clear();
    size = 0;
  }

  public void flush() throws Exception {
    if (data.size() < 1) { return; }
    final long flushStartTime = System.currentTimeMillis();
    final WritableEntity entity = new WritableEntity(getContentType(), encoding, deflate);
    final OutputStream os = entity.openBuffer();
    final int events = data.size();
    if (header != null) os.write(header);
    boolean first = true;
    for (final BulkEntry e : data) {
      if (!first && separator != null) os.write(separator);
      final String dataReq = e.bulkEntry();
      BulkSender.logger.debug(dataReq);
      os.write(dataReq.getBytes(encoding));
      first = false;
    }
    if (footer != null) os.write(footer);
    entity.closeBuffer();
    final BulkSenderThread sender = new BulkSenderThread(this, entity, events);
    stats.addFlushTime(System.currentTimeMillis() - flushStartTime);
    if (numThreads > 1) {
      BulkSender.logger.debug("HTTP sender taskCount:" + senderPool.getTaskCount() + " queueSize: " + senderPool.getQueue().size() + " activeCount:" + senderPool.getActiveCount());
      senderPool.execute(sender);
    }
    else {
      sender.run();
    }
    data.clear();
    size = 0;
  }

  public void close() throws Exception {
    BulkSender.logger.debug("Closing");
    flush();
  }

}
