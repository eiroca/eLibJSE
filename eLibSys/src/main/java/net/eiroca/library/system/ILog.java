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
package net.eiroca.library.system;

public interface ILog {

  enum LogLevel {
    trace, debug, info, warn, error, fatal
  }

  public void log(final LogLevel priority, final String msg);

  public void logF(final LogLevel priority, final String format, Object... args);

  public boolean isLoggable(final LogLevel level);

  public void trace(final Object... msg);

  public void debug(final Object... msg);

  public void info(final Object... msg);

  public void warn(final Object... msg);

  public void error(final Object... msg);

  public void fatal(final Object... msg);

}
