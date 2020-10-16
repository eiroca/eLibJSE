/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.scheduler;

import java.util.concurrent.TimeUnit;

public class FixedFrequencyPolicy implements SchedulerPolicy {

  long freq;

  @Override
  public long next(final long lastTime) {
    return ((int)(lastTime / freq) + 1) * freq;
  }

  public FixedFrequencyPolicy(final long freq, final long unit, final TimeUnit tu) {
    super();
    this.freq = tu.toMillis(unit) / freq;
  }

  public FixedFrequencyPolicy(final long unit, final TimeUnit tu) {
    super();
    freq = tu.toMillis(unit);
  }

}
