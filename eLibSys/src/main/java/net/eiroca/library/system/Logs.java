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
package net.eiroca.library.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logs {

  private static final Logger logger = Logs.getLogger();

  public static void ignore(final Exception e) {
    Logs.logger.trace("Ingoring", e);
  }

  public static Logger getLogger() {
    final String name = Thread.currentThread().getStackTrace()[2].getClassName();
    return Logs.getLogger(name);
  }

  public static Logger getLogger(final int level) {
    final String name = Thread.currentThread().getStackTrace()[level].getClassName();
    return Logs.getLogger(name);
  }

  public static Logger getLogger(final String name) {
    final Logger logger = LoggerFactory.getLogger(name);
    return logger;
  }
}
