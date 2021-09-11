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
package net.eiroca.library.util.impl;

import net.eiroca.library.util.Counter;

/**
 * The class implements a counter of "errors" and "successes". The counter has a state that changes
 * according the occurrences of the errors and successes.
 */
public class BaseCounter implements Counter {

  private long lastError = 0;
  private long errCount = 0;
  private long errSeq = 0;

  private long lastSuccess = 0;
  private long sucCount = 0;
  private long sucSeq = 0;

  private int maxSeqErr = -1;
  private int maxSeqSuc = -1;

  public final static int ST_UNKNOWN = 0;
  public final static int ST_OK = 1;
  public final static int ST_ERROR = 2;

  private int status = BaseCounter.ST_UNKNOWN;
  private int lastStatus = BaseCounter.ST_UNKNOWN;

  public BaseCounter(final int maxSeqErr, final int maxSeqSuc) {
    this.maxSeqErr = maxSeqErr;
    this.maxSeqSuc = maxSeqSuc;
  }

  public long getLastError() {
    return lastError;
  }

  @Override
  public int getLastStatus() {
    return lastStatus;
  }

  public long getLastSuccess() {
    return lastSuccess;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public synchronized void reset() {
    lastError = 0;
    errCount = 0;
    errSeq = 0;
    lastSuccess = 0;
    sucCount = 0;
    sucSeq = 0;
    maxSeqErr = -1;
    maxSeqSuc = -1;
    status = BaseCounter.ST_UNKNOWN;
    lastStatus = BaseCounter.ST_UNKNOWN;
  }

  @Override
  public String toString() {
    return new StringBuilder().append("State (").append(status).append(") Error=").append(errCount).append(" Success=").append(sucCount).toString();
  }

  /**
   * Increments the counter according to the given parameter and checks if the state must change.
   * @param err If true the events is an error
   * @return The state of the counter
   */
  @Override
  public synchronized void touch(final boolean err) {
    if (err) {
      lastError = System.currentTimeMillis();
      errCount++;
    }
    else {
      lastSuccess = System.currentTimeMillis();
      sucCount++;
    }
    lastStatus = status;
    updateStatus(err);
  }

  /**
   * Check if there is a status change.
   * @param err If true the last events was an error
   * @return The new state of the counter
   */
  private void updateStatus(final boolean err) {
    if (err) {
      switch (status) {
        case ST_UNKNOWN: {
          errSeq++;
          sucSeq = 0;
          if (errSeq > maxSeqErr) {
            status = BaseCounter.ST_ERROR;
            errSeq = 0;
          }
          break;
        }
        case ST_OK: {
          status = BaseCounter.ST_UNKNOWN;
          errSeq = 1;
          if (errSeq > maxSeqErr) {
            status = BaseCounter.ST_ERROR;
            errSeq = 0;
          }
          break;
        }
        case ST_ERROR: {
          break;
        }
      }
    }
    else {
      switch (status) {
        case ST_UNKNOWN: {
          sucSeq++;
          errSeq = 0;
          if (sucSeq > maxSeqSuc) {
            status = BaseCounter.ST_OK;
            sucSeq = 0;
          }
          break;
        }
        case ST_OK: {
          break;
        }
        case ST_ERROR: {
          status = BaseCounter.ST_UNKNOWN;
          sucSeq = 1;
          if (sucSeq > maxSeqSuc) {
            status = BaseCounter.ST_OK;
            sucSeq = 0;
          }
          break;
        }
      }
    }
  }

}
