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
package net.eiroca.library.diagnostics.monitors;

import net.eiroca.library.db.DBConfig;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.SQLchecks;
import net.eiroca.library.system.IContext;

public class OracleMonitor extends DatabaseMonitor {

  private static final String CONFIG_ORACLEVERSION = "OracleVersion";

  @Override
  public void readConf() throws CommandException {
    super.readConf();
    config.setSQLType(DBConfig.TYPE_ORACLE);
    final String version = context.getConfigString(OracleMonitor.CONFIG_ORACLEVERSION, "");
    dbVariant = (version.contains("later") || version.contains("grather")) ? "_v11" : "";
  }

  @Override
  public void setup(final IContext context) throws CommandException {
    super.setup(context);
    checks = new SQLchecks[] {
        new SQLchecks("Oracle Statistics", "Oracle - {0}", "/res/oracle_metrics.sql"), //
        new SQLchecks("Oracle Statistics", "Oracle - Tablespace - {0}", "/res/oracle_tablespaces.sql", 2, "Tablespace"), //
        new SQLchecks("Oracle Statistics", "Oracle - Waiter - {0}", "/res/oracle_waiters.sql", 2, "Waiter"), //
        new SQLchecks("Oracle Statistics", "Oracle - Lock - {0}", "/res/oracle_locks.sql", 2, "Lock"), //
        new SQLchecks("Oracle Statistics", "Oracle - Top SQL - {0}", "/res/oracle{0}_topsql.sql", 2, "Query")//
    };
  }

}
