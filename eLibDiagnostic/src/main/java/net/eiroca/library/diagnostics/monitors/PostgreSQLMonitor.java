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

public class PostgreSQLMonitor extends DatabaseMonitor {

  @Override
  public void readConf() throws CommandException {
    super.readConf();
    config.setSQLType(DBConfig.TYPE_POSTGRES);
    sqlParams = new String[] {
        config.getDatabase()
    };
  }

  @Override
  public void setup(final IContext context) throws CommandException {
    super.setup(context);
    checks = new SQLchecks[] {
        new SQLchecks("PostgreSQL Statistics", "PostgreSQL - {0}", "/res/postgres_metrics.sql"), //
        new SQLchecks("PostgreSQL Statistics", "PostgreSQL - Tables - {0}", "/res/postgres_table.sql", 2, "Table"), //
        new SQLchecks("PostgreSQL Statistics", "PostgreSQL - Indexes - {0}", "/res/postgres_index.sql", 2, "Index"), //
        new SQLchecks("PostgreSQL Statistics", "PostgreSQL - Sequences - {0}", "/res/postgres_sequence.sql", 2, "Sequence"), //
    };
  }

}
