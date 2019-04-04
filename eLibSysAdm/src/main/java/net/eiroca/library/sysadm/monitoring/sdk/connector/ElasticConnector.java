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
package net.eiroca.library.sysadm.monitoring.sdk.connector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import net.eiroca.ext.library.elastic.ElasticBulk;
import net.eiroca.ext.library.gson.SimpleJson;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.sysadm.monitoring.api.IConnector;
import net.eiroca.library.system.ContextParameters;
import net.eiroca.library.system.IContext;

public class ElasticConnector implements IConnector {

  public static final String ID = "elastic";
  //
  public static ContextParameters config = new ContextParameters();
  //
  public static StringParameter _elasticURL = new StringParameter(ElasticConnector.config, "elasticURL", "http://localhost:9200/_bulk");
  public static StringParameter _elasticIndex = new StringParameter(ElasticConnector.config, "elasticIndex", "metrics-");
  public static IntegerParameter _elasticIndexMode = new IntegerParameter(ElasticConnector.config, "elasticIndexMode", 1, 0, 2);
  public static StringParameter _indexDateFormat = new StringParameter(ElasticConnector.config, "indexDateFormat", "yyyy.MM.dd");
  public static StringParameter _elasticType = new StringParameter(ElasticConnector.config, "elasticType", "metric");
  public static StringParameter _elasticPipeline = new StringParameter(ElasticConnector.config, "elasticPipeline", null);
  // Dynamic mapped to parameters
  protected String config_elasticURL;
  protected String config_elasticIndex;
  protected int config_elasticIndexMode;// 0 fixed, 1 fixed+now 2 fixed+event.date
  protected String config_indexDateFormat;
  protected String config_elasticType;
  protected String config_elasticPipeline;
  //
  protected IContext context = null;
  protected ElasticBulk elasticServer = null;
  protected SimpleDateFormat indexDateFormat;

  public ElasticConnector() {
    super();
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public void setup(final IContext context) throws Exception {
    this.context = context;
    ElasticConnector.config.convert(context, null, this, "config_");
    indexDateFormat = new SimpleDateFormat(config_indexDateFormat);
    elasticServer = LibStr.isNotEmptyOrNull(config_elasticURL) ? new ElasticBulk(config_elasticURL) : null;
    if (elasticServer != null) {
      elasticServer.open();
    }
    context.info(this.getClass().getName(), " setup done");
  }

  @Override
  public void teardown() throws Exception {
    context.info(this.getClass().getName(), " teardown");
    if (elasticServer != null) {
      elasticServer.close();
      elasticServer = null;
    }
  }

  @Override
  public boolean beginBulk() {
    return (elasticServer != null);
  }

  @Override
  public void endBulk() {
    try {
      elasticServer.flush();
    }
    catch (Exception e) {
      context.error("Error flushing to elastic: " + e.getMessage(), e);
    }
  }

  @Override
  public void process(Event event) {
    final SimpleJson json = event.getData();
    final String _doc = json.toString();
    try {
      final String _id = getEventID(event);
      final String _indexName = getEventIndex(event);
      elasticServer.add(_indexName, config_elasticType, _id, config_elasticPipeline, _doc);
    }
    catch (final Exception e) {
      context.error("Error exporting ", _doc, "->", e.getMessage(), " ", Helper.getStackTraceAsString(e));
    }
  }

  private String getEventIndex(final Event event) {
    final long timestamp = event.getTimestamp();
    String index;
    switch (config_elasticIndexMode) {
      case 1:
        index = config_elasticIndex + indexDateFormat.format(new Date());
        break;
      case 2:
        index = config_elasticIndex + indexDateFormat.format(new Date(timestamp));
        break;
      default:
        index = config_elasticIndex;
        break;
    }
    return index;
  }

  private String getEventID(final Event event) {
    return UUID.randomUUID().toString();
  }

}
