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
package net.eiroca.sysadm.diagnostics;

import java.util.ArrayList;
import java.util.List;
import net.eiroca.library.diagnostics.monitors.RedisMonitor;
import net.eiroca.library.diagnostics.monitors.TCPServerMonitor;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricGroup;
import net.eiroca.library.system.Context;

public class TestMonitor {

  public static void main(final String[] args) {
    final Context context = Context.current();
    final TCPServerMonitor monitor = new RedisMonitor();
    context.setConfig(RedisMonitor.CONFIG_PORT, "6379");
    context.setConfig(RedisMonitor.CONFIG_AUTH, null);
    try {
      monitor.setup(context);
      monitor.check("it255avr.it.sedc.internal.vodafone.com");
      final List<MetricGroup> groups = new ArrayList<>();
      monitor.loadMetricGroup(groups);
      for (final MetricGroup g : groups) {
        context.debug("processing group: ", g.getName());
        g.refresh();
        for (final Measure m : g.getMetrics()) {
          context.info(m.toString());
        }
      }
      monitor.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

  }

}
