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
package net.eiroca.library.util;

public interface Context {

  // Configuration
  public void declare(String propName, String propDefault);

  public void error(String why, Exception err);

  // Logging
  public void fatal(String why, Exception err);

  public Counter getCounter(String name);

  public String getFullName();

  public int getInt(String propName);

  public String getName();

  public Context getParent();

  public String getString(String propName);

  public Context getSubContext(String name);

  public void success(String why);

  public void warning(String why, Exception err);

}
