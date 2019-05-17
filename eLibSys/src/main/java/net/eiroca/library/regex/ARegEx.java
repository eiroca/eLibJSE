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
package net.eiroca.library.regex;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import net.eiroca.library.system.Logs;

public abstract class ARegEx {

  transient protected static final Logger logger = Logs.getLogger();

  public int count = 0;
  // time in nanoseconds
  public long totalTime = 0;
  protected int sizeLimit = 0;
  protected long timeLimit = Long.MAX_VALUE;
  public int sizeMin = 0;
  public String pattern = null;

  private long now;
  private long elapsed;

  public ARegEx(final String pattern) {
    this.pattern = pattern;
  }

  public ARegEx setLimit(int sizeLimit) {
    if (sizeLimit < 0) {
      sizeLimit = -1;
    }
    this.sizeLimit = sizeLimit;
    return this;
  }

  protected void timeFail() {
    ARegEx.logger.info("REGEX SLOW: {}", pattern);
    updateLimit();
  }

  protected void ruleFail() {
    ARegEx.logger.warn("REGEX FAIL: {}", pattern);
    updateLimit();
  }

  protected void updateLimit() {
    if (sizeLimit < 1) { return; }
    final int theMinSize = Math.min(sizeMin, sizeLimit);
    final int newLimit = Math.max(sizeLimit / 2, theMinSize);
    if (sizeLimit != newLimit) {
      ARegEx.logger.info("REGEX LIMITED to {}: {}", newLimit, pattern);
      sizeLimit = newLimit;
    }
  }

  abstract public boolean find(String text);

  abstract public String findFirst(String text);

  abstract public List<String> extract(List<String> namedFields, String text);

  abstract public List<String> extract(String text);

  public void setSizeLimit(final int sizeLimit) {
    this.sizeLimit = sizeLimit;
  }

  public void setSizeMin(final int sizeMin) {
    this.sizeMin = sizeMin;
  }

  public void setTimeLimit(final int duration, final TimeUnit tu) {
    timeLimit = tu.toNanos(duration);
  }

  protected void tic() {
    count++;
    now = System.nanoTime();
  }

  protected void toc(final boolean success) {
    elapsed = (System.nanoTime() - now);
    totalTime += elapsed;
    if (success && (elapsed > timeLimit)) {
      timeFail();
    }
  }

}
