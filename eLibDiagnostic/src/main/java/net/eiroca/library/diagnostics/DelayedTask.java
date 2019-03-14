/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio)
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
package net.eiroca.library.diagnostics;

public abstract class DelayedTask implements ITask {

  protected long lastRun = 0;
  protected long delay;

  public DelayedTask(final long delay) {
    super();
    this.delay = delay;
  }

  public long getDelay() {
    return delay;
  }

  public void setDelay(final long delay) {
    this.delay = delay;
  }

  public long getLastRun() {
    return lastRun;
  }

  @Override
  public boolean execute() throws CommandException {
    final long now = System.currentTimeMillis();
    if ((now - lastRun) >= delay) {
      lastRun = now;
      return internalExecute();
    }
    return true;
  }

  public abstract boolean internalExecute() throws CommandException;

}
