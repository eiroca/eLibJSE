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
package net.eiroca.library.sysadm.monitoring.sdk.exporter;

import org.slf4j.Logger;
import net.eiroca.ext.library.gson.SimpleJson;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.sysadm.monitoring.api.IConnector;
import net.eiroca.library.system.ContextParameters;
import net.eiroca.library.system.IContext;
import net.eiroca.library.system.Logs;

public class LoggerExporter implements IConnector {

  private static final String CONFIG_PREFIX = null;
  public static final String ID = "logger";
  //
  public static ContextParameters config = new ContextParameters();
  //
  public static StringParameter _logger = new StringParameter(LoggerExporter.config, "logger", "Metrics");
  // Dynamic mapped to parameters
  protected String config_logger;
  //
  protected IContext context = null;
  protected Logger metricLog = null;

  public LoggerExporter() {
    super();
  }

  @Override
  public String getId() {
    return LoggerExporter.ID;
  }

  @Override
  public void setup(final IContext context) throws Exception {
    this.context = context;
    LoggerExporter.config.convert(context, LoggerExporter.CONFIG_PREFIX, this, "config_");
    metricLog = LibStr.isNotEmptyOrNull(config_logger) ? Logs.getLogger(config_logger) : null;
    context.info(this.getClass().getName(), " setup done");
  }

  @Override
  public void teardown() throws Exception {
    context.info(this.getClass().getName(), " teardown");
  }

  @Override
  public void process(final Event event) {
    final SimpleJson json = event.getData();
    final String _doc = json.toString();
    metricLog.info(_doc);
  }

  @Override
  public boolean beginBulk() {
    return (metricLog != null);
  }

  @Override
  public void endBulk() {
  }

}
