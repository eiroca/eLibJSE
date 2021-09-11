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
package net.eiroca.library.sysadm.monitoring.sdk.exporter;

import java.util.ArrayList;
import java.util.List;
import net.eiroca.library.core.Registry;
import net.eiroca.library.sysadm.monitoring.api.IExporter;
import net.eiroca.library.system.Logs;

public class Exporters {

  public static final Registry registry = new Registry();

  public static final List<String> defExporters = new ArrayList<>();

  static {
    Exporters.defExporters.add(ElasticExporter.ID);
    Exporters.defExporters.add(LoggerExporter.ID);
  }

  static {
    Exporters.registry.addEntry(ElasticExporter.ID, ElasticExporter.class.getName());
    Exporters.registry.addEntry(LoggerExporter.ID, LoggerExporter.class.getName());
    Exporters.registry.addEntry(NotifyExporter.ID, NotifyExporter.class.getName());
  }

  public static IExporter newInstance(final String name) {
    IExporter obj = null;
    try {
      obj = (IExporter)Class.forName(Exporters.registry.className(name)).newInstance();
    }
    catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      Logs.ignore(e);
    }
    return obj;
  }

}
