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
package net.eiroca.library.diagnostics;

import net.eiroca.library.core.Registry;
import net.eiroca.library.diagnostics.actions.LocalCommandAction;
import net.eiroca.library.diagnostics.actions.SSHCommandAction;
import net.eiroca.library.diagnostics.actions.WebServiceAction;
import net.eiroca.library.diagnostics.monitors.ApacheServerMonitor;
import net.eiroca.library.diagnostics.monitors.DatabaseMonitor;
import net.eiroca.library.diagnostics.monitors.DatapowerMonitor;
import net.eiroca.library.diagnostics.monitors.ElasticSearchMonitor;
import net.eiroca.library.diagnostics.monitors.FLUMEServerMonitor;
import net.eiroca.library.diagnostics.monitors.GraphiteMonitor;
import net.eiroca.library.diagnostics.monitors.OracleMonitor;
import net.eiroca.library.diagnostics.monitors.PostgreSQLMonitor;
import net.eiroca.library.diagnostics.monitors.RedisMonitor;
import net.eiroca.library.diagnostics.monitors.TCPServerMonitor;
import net.eiroca.library.diagnostics.monitors.WebServerMonitor;
import net.eiroca.library.diagnostics.monitors.eSysAdmServerMonitor;

public class ServerMonitors {

  public static final Registry registry = new Registry();

  static {
    ServerMonitors.registry.addEntry("TCP Server", TCPServerMonitor.class.getName());
    ServerMonitors.registry.addEntry("Web Server", WebServerMonitor.class.getName());
    ServerMonitors.registry.addEntry("Apache Web Server", ApacheServerMonitor.class.getName());
    ServerMonitors.registry.addEntry("eSysAdm Server", eSysAdmServerMonitor.class.getName());
    ServerMonitors.registry.addEntry("FLUME Server", FLUMEServerMonitor.class.getName());
    ServerMonitors.registry.addEntry("ElasticSearch Server", ElasticSearchMonitor.class.getName());
    ServerMonitors.registry.addEntry("Database Server", DatabaseMonitor.class.getName());
    ServerMonitors.registry.addEntry("Oracle Database Server", OracleMonitor.class.getName());
    ServerMonitors.registry.addEntry("PostgreSQL Server", PostgreSQLMonitor.class.getName());
    ServerMonitors.registry.addEntry("Redis Server", RedisMonitor.class.getName());
    ServerMonitors.registry.addEntry("WebService", WebServiceAction.class.getName());
    ServerMonitors.registry.addEntry("SSH command", SSHCommandAction.class.getName());
    ServerMonitors.registry.addEntry("Local command", LocalCommandAction.class.getName());
    ServerMonitors.registry.addEntry("Graphite Server", GraphiteMonitor.class.getName());
    ServerMonitors.registry.addEntry("DataPower Server", DatapowerMonitor.class.getName());
  }

  public static IServerMonitor build(final String name) throws Exception {
    String clazzName = ServerMonitors.registry.className(name);
    if (clazzName == null) {
      clazzName = name;
    }
    final IServerMonitor obj = (IServerMonitor)Class.forName(clazzName).newInstance();
    return obj;
  }

}
