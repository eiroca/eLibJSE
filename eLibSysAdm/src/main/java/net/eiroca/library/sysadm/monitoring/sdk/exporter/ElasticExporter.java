/**
 *
 * Copyright (C) 1999-2025 Enrico Croce - AGPL >= 3.0
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

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.UUID;
import net.eiroca.ext.library.elastic.ElasticBulk;
import net.eiroca.ext.library.gson.SimpleGson;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.system.IContext;

public class ElasticExporter extends GenericExporter {

  private static final Encoder BASE64ENCODER = Base64.getEncoder();

  public static final String ID = "elastic".toLowerCase();
  //
  public static StringParameter _elasticURL = new StringParameter(ElasticExporter.config, "elasticURL", null);
  public static StringParameter _elasticUsername = new StringParameter(ElasticExporter.config, "elasticUsername", null);
  public static StringParameter _elasticPassword = new StringParameter(ElasticExporter.config, "elasticPassword", null);
  public static StringParameter _elasticIndex = new StringParameter(ElasticExporter.config, "elasticIndex", "metrics-");
  public static IntegerParameter _elasticIndexMode = new IntegerParameter(ElasticExporter.config, "elasticIndexMode", 1, 0, 2);
  public static StringParameter _indexDateFormat = new StringParameter(ElasticExporter.config, "indexDateFormat", "yyyy.MM.dd");
  public static StringParameter _elasticType = new StringParameter(ElasticExporter.config, "elasticType", "metric");
  public static IntegerParameter _elasticVersion = new IntegerParameter(ElasticExporter.config, "elasticVersion", 7);
  public static StringParameter _elasticPipeline = new StringParameter(ElasticExporter.config, "elasticPipeline", null);
  // Dynamic mapped to parameters
  protected String config_elasticURL;
  protected String config_elasticUsername;
  protected String config_elasticPassword;
  protected String config_elasticIndex;
  protected int config_elasticIndexMode;// 0 fixed, 1 fixed+now 2 fixed+event.date
  protected String config_indexDateFormat;
  protected String config_elasticType;
  protected int config_elasticVersion;
  protected String config_elasticPipeline;
  //
  protected ElasticBulk elasticServer = null;
  protected SimpleDateFormat indexDateFormat;

  public ElasticExporter() {
    super();
  }

  @Override
  public void setup(final IContext context) throws Exception {
    super.setup(context);
    GenericExporter.config.convert(context, GenericExporter.CONFIG_PREFIX, this, "config_");
    indexDateFormat = new SimpleDateFormat(config_indexDateFormat);
    elasticServer = LibStr.isNotEmptyOrNull(config_elasticURL) ? new ElasticBulk(config_elasticURL, config_elasticVersion) : null;
    String token = null;
    if (elasticServer != null) {
      if (config_elasticUsername != null) {
        final String credential = config_elasticUsername + ":" + config_elasticPassword;
        final String auth = ElasticExporter.BASE64ENCODER.encodeToString(credential.getBytes());
        token = auth.substring(0, 8);
        elasticServer.setAuthorization("Basic " + auth);
      }
      elasticServer.open();
    }
    context.info(this.getClass().getName(), " setup done, url=", config_elasticURL, " token=", token);
  }

  @Override
  public void teardown() throws Exception {
    super.teardown();
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
    catch (final Exception e) {
      context.error("Error flushing to elastic: " + e.getMessage(), e);
    }
  }

  @Override
  public void process(final Event event) {
    final SimpleGson json = event.getData();
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
    String index;
    switch (config_elasticIndexMode) {
      case 1:
        index = config_elasticIndex + indexDateFormat.format(new Date());
        break;
      case 2:
        final long timestamp = event.getTimestamp();
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

  @Override
  public String getId() {
    return ElasticExporter.ID;
  }

}
