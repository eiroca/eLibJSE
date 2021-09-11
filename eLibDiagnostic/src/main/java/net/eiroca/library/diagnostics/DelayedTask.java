/**
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
package net.eiroca.library.diagnostics;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import net.eiroca.library.config.Parameters;
import net.eiroca.library.core.Helper;
import net.eiroca.library.system.Logs;

public abstract class DelayedTask implements ITask {

  final protected static transient Logger logger = Logs.getLogger();

  final protected transient Parameters params = new Parameters();

  protected long lastRun = 0;
  protected long delay;

  public DelayedTask() {
    this(0);
  }

  public DelayedTask(final long delay) {
    super();
    this.delay = delay;
    params.setName(getClass().getName());
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
      return run();
    }
    return true;
  }

  public void loadConf(final String path) {
    Properties p;
    try {
      p = Helper.loadProperties(path, false);
    }
    catch (final IOException e) {
      DelayedTask.logger.info("Unable to load config file: " + path);
      p = new Properties();
    }
    params.loadConfig(p, null);
    params.saveConfig(this, "config_", true, true);
    DelayedTask.logger.info(params.toString());
  }

  public abstract boolean run() throws CommandException;

}
