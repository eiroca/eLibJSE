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

import net.eiroca.ext.library.gson.GsonUtil;

public class ElasticBulkStats {

  private long event_count;
  private long event_sent;
  private long event_errors;

  private long addTime;
  private long flushTime;
  private long sendTime;
  private long checkTime;

  public synchronized void reset() {
    event_count = 0;
    event_sent = 0;
    event_errors = 0;
    addTime = 0;
    flushTime = 0;
    sendTime = 0;
    checkTime = 0;
  }

  public long getEventCount() {
    return event_count;
  }

  public long getEventSent() {
    return event_sent;
  }

  public long getEventErrors() {
    return event_errors;
  }

  public long getAddTime() {
    return addTime;
  }

  public long getFlushTime() {
    return flushTime;
  }

  public long getSendTime() {
    return sendTime;
  }

  public long getCheckTime() {
    return checkTime;
  }

  public synchronized void incEventCount(final int events) {
    event_count += events;
  }

  public synchronized void incEventSent(final int events) {
    event_sent += events;
  }

  public synchronized void incEventErrors(final int events) {
    event_errors += events;
  }

  public synchronized void addAddTime(final long time) {
    addTime += time;
  }

  public synchronized void addFlushTime(final long time) {
    flushTime += time;
  }

  public synchronized void addSendTime(final long time) {
    sendTime += time;
  }

  public synchronized void addCheckTime(final long time) {
    checkTime += time;
  }

  @Override
  public String toString() {
    return GsonUtil.toJSON(this);
  }

}
