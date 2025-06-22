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
package net.eiroca.ext.library.dynatrace;

import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import net.eiroca.ext.library.http.bulk.BulkEntry;
import net.eiroca.ext.library.http.bulk.BulkSender;
import net.eiroca.library.system.Logs;

public class DynatraceBulk extends BulkSender<BulkEntry> {

  private static final Logger logger = Logs.getLogger();

  private static final int DEFAULT_THREADS = 1;
  private static final int DEFAULT_BULKSIZE = 16 * 1024;

  public DynatraceBulk(final String server, final int version) {
    this(server, version, DynatraceBulk.DEFAULT_BULKSIZE, DynatraceBulk.DEFAULT_THREADS);
  }

  public DynatraceBulk(final String server, final int version, final int bulkSize, final int numThreads) {
    super(server, version, bulkSize, numThreads);
    setContentType("text/plain");
    setAcceptType("application/json");
  }

  @Override
  public boolean checkValidResponse(final int responseCode, final HttpEntity entity, final int size) {
    int errors = 0;
    if (responseCode == 400) {
      // partial failure
      errors = size;
    }
    else if (responseCode >= 500) {
      // server failure
      errors = size;
    }
    else if (responseCode >= 401) {
      // client failure
      errors = size;
    }
    stats.incEventErrors(errors);
    return errors == 0;
  }

  @Override
  public void setOverload(final boolean overload) {
  }

  @Override
  public boolean isOverload() {
    return false;
  }

  public void add(final String data, final String meta) throws Exception {
    final long now = System.currentTimeMillis();
    final BulkEntry e = new BulkEntry(data, meta);
    size += e.entrySize();
    this.data.add(e);
    stats.incEventCount(1);
    stats.addAddTime(System.currentTimeMillis() - now);
    if (size > getBulkSize()) {
      DynatraceBulk.logger.debug("Internal flush size: {}", size);
      flush();
    }
  }

}
