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
import net.eiroca.library.diagnostics.actiondata.ActionData;
import net.eiroca.library.diagnostics.actions.BaseAction;
import net.eiroca.library.diagnostics.actions.SSHCommandAction;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MeasureGroup;
import net.eiroca.library.system.Context;

public class TestAction {

  public static void main(final String[] args) {
    final Context context = Context.current();
    context.setConfig("param", "cmd");
    // context.setConfig("command", "cmd /c echo ${ARGS_0}=10;2");
    context.setConfig("command", "echo '${PARAM}=10;2'");
    context.setConfig("command", "logger –p local1.err hpovo: trigger_off major EasyTicket5 MERLINO app_vfit-bse-sales-dealer-auto \"TEST - Easyticket ticket merlino - 003\"");
    context.setConfig("metric_prefix", "cmd=");
    context.setConfig("metric_names", "command;index");
    context.setConfig("matchContent", "regex");
    context.setConfig("regex", ".*cmd.*");

    context.setConfig("authMethod", "Password");
    context.setConfig("username", "wwwadm");
    context.setConfig("password", "ambsesaleswm!");

    context.setConfig("host", "it142aia.it.sedc.internal.vodafone.com");
    context.setConfig("port", "22");

    final BaseAction action = new SSHCommandAction();
    try {
      action.setup(context);
      final ActionData data = new ActionData();
      final ReturnObject res = action.execute(data);
      if (res != null) {
        context.info("RC=" + res.getRetCode());
        context.info("output=" + res.getOutput());
      }
      final List<MeasureGroup> groups = new ArrayList<>();
      action.loadMetricGroup(groups);
      for (final MeasureGroup g : groups) {
        context.debug("processing group: ", g.getName());
        g.refresh();
        for (final Measure m : g.getMetrics()) {
          context.info(m.toString());
        }
      }
      action.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

  }

}
