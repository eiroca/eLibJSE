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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.eiroca.ext.library.dynatrace.DynatraceBulk;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.sysadm.monitoring.sdk.MeasureFields;
import net.eiroca.library.system.IContext;

public class DynatraceMetricExporter extends GenericExporter {

  public static final String ID = "dynatrace".toLowerCase();
  //
  public static StringParameter _dynatraceURL = new StringParameter(DynatraceMetricExporter.config, "dynatraceURL", null);
  public static IntegerParameter _dynatraceVersion = new IntegerParameter(ElasticExporter.config, "dynatraceVersion", 1);
  public static StringParameter _dynatraceToken = new StringParameter(DynatraceMetricExporter.config, "dynatraceToken", null);
  public static StringParameter _dynatraceMetric = new StringParameter(DynatraceMetricExporter.config, "dynatraceMetric", //
      // 0..5 metric name, 6..11 dimension name, 12..17 value
      // FLD_SOURCE[0,6,12], FLD_GROUP[1,7,13], FLD_METRIC[2,8,14], FLD_HOST[3,9,15], FLD_SPLIT_GROUP[4,10,16], FLD_SPLIT_NAME[5,11,17]
      "net.eiroca.{0}.{2},group=\"{13}\",hostname=\"{15}\"");
  public static StringParameter _dynatraceMetricSplitted = new StringParameter(DynatraceMetricExporter.config, "dynatraceMetricSplitted", //
      // 0..5 metric name, 6..11 dimension name, 12..17 value
      // FLD_SOURCE[0,6,12], FLD_GROUP[1,7,13], FLD_METRIC[2,8,14], FLD_HOST[3,9,15], FLD_SPLIT_GROUP[4,10,16], FLD_SPLIT_NAME[5,11,17]
      "net.eiroca.{0}.{2},group=\"{13}\",hostname=\"{15}\",{10}=\"{17}\",split=\"1\"");
  // Dynamic mapped to parameters
  protected String config_dynatraceURL;
  protected String config_dynatraceToken;
  protected String config_dynatraceMetric;
  protected String config_dynatraceMetricSplitted;
  //
  protected DynatraceBulk dynatraceServer = null;
  protected int config_dynatraceVersion;
  protected SimpleDateFormat indexDateFormat;

  public DynatraceMetricExporter() {
    super();
  }

  @Override
  public void setup(final IContext context) throws Exception {
    super.setup(context);
    GenericExporter.config.convert(context, GenericExporter.CONFIG_PREFIX, this, "config_");
    dynatraceServer = LibStr.isNotEmptyOrNull(config_dynatraceURL) ? new DynatraceBulk(config_dynatraceURL, config_dynatraceVersion) : null;
    String token = null;
    if (dynatraceServer != null) {
      if (config_dynatraceToken != null) {
        token = config_dynatraceToken.substring(0, 8);
        dynatraceServer.setAuthorization("Api-Token " + config_dynatraceToken);
      }
      dynatraceServer.open();
    }
    context.info(this.getClass().getName(), " setup done, url=", config_dynatraceURL, " token=", token);
  }

  @Override
  public void teardown() throws Exception {
    super.teardown();
    if (dynatraceServer != null) {
      dynatraceServer.close();
      dynatraceServer = null;
    }
  }

  @Override
  public boolean beginBulk() {
    return (dynatraceServer != null);
  }

  @Override
  public void endBulk() {
    try {
      dynatraceServer.flush();
    }
    catch (final Exception e) {
      context.error("Error flushing to elastic: " + e.getMessage(), e);
    }
  }

  @Override
  public void process(final Event event) {
    GenericExporter.logger.info("Dynatrace exporter processing");
    String _doc = "??ERR??";
    try {
      _doc = formatMetric(event);
      GenericExporter.logger.debug("Dynatrace Metric: {}", _doc);
      dynatraceServer.add(_doc, null);
    }
    catch (final Exception e) {
      context.error("Error exporting ", _doc, "->", e.getMessage(), " ", Helper.getStackTraceAsString(e));
    }
  }

  //{"@timestamp":"2025-06-11T12:50:32.235+02:00","group":"DS","host":"10.109.42.149","metric":"Metrics","source":"eSysAdmServer","split":"true","splitGroup":"Applicativi","splitName":"Device con importo anomalo","value":0.0}

  private String formatMetric(final Event event) {
    final JsonObject json = event.getData().getRoot();
    GenericExporter.logger.trace("Raw metric: {}", json);
    final String[] value = new String[6];
    final String[] metrics = new String[6];
    final String[] dimensions = new String[6];
    value[0] = DynatraceMetricExporter.getIt(json, MeasureFields.FLD_SOURCE, "source");
    value[1] = DynatraceMetricExporter.getIt(json, MeasureFields.FLD_GROUP, "group");
    value[2] = DynatraceMetricExporter.getIt(json, MeasureFields.FLD_METRIC, "metric");
    value[3] = DynatraceMetricExporter.getIt(json, MeasureFields.FLD_HOST, "host");
    value[4] = DynatraceMetricExporter.getIt(json, MeasureFields.FLD_SPLIT_GROUP, "splitGroup");
    value[5] = DynatraceMetricExporter.getIt(json, MeasureFields.FLD_SPLIT_NAME, "splitName");
    convertIt(value, metrics, dimensions);
    final String split = DynatraceMetricExporter.getIt(json, MeasureFields.FLD_SPLIT, "false");
    String metricFormat;
    if ((split != null) && "true".equals(split)) {
      metricFormat = config_dynatraceMetricSplitted;
    }
    else {
      metricFormat = config_dynatraceMetric;
    }
    final StringBuffer row = new StringBuffer(256);
    row.append(MessageFormat.format(metricFormat, metrics[0], metrics[1], metrics[2], metrics[3], metrics[4], metrics[5], dimensions[0], dimensions[1], dimensions[2], dimensions[3], dimensions[4], dimensions[5], value[0], value[1], value[2], value[3], value[4], value[5]));
    row.append(' ');
    row.append("gauge,");
    DynatraceMetricExporter.appendDouble(row, json, MeasureFields.FLD_VALUE);
    row.append(' ');
    return row.toString();
  }

  private void convertIt(final String[] value, final String[] metrics, final String[] dimensions) {
    for (int i = 0; i < value.length; i++) {
      final String v = value[i];
      final StringBuffer met = new StringBuffer(v.length());
      final StringBuffer dim = new StringBuffer(v.length());
      for (int l = 0; l < v.length(); l++) {
        final char c = v.charAt(l);
        if (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z'))) {
          met.append(c);
          dim.append(c);
        }
        else if ((c >= 'A') && (c <= 'Z')) {
          met.append(c);
          dim.append(Character.isLowerCase(c));
        }
        else {
          switch (c) {
            case ' ': {
              met.append('_');
              dim.append('_');
              break;
            }
            case 'à': {
              met.append('a');
              dim.append('a');
              break;
            }
            case 'è':
            case 'é': {
              met.append('e');
              dim.append('e');
              break;
            }
            case 'ì': {
              met.append('i');
              dim.append('i');
              break;
            }
            case 'ò': {
              met.append('o');
              dim.append('o');
              break;
            }
            case 'ù': {
              met.append('u');
              dim.append('u');
              break;
            }
            case '.':
            case ':': {
              met.append('_');
              dim.append(c);
              break;
            }
            case '_':
            case '-': {
              met.append(c);
              dim.append(c);
              break;
            }
          }
        }
      }
      metrics[i] = trim(met.toString());
      dimensions[i] = trim(dim.toString());
    }
  }

  private static boolean isLetter(final char c) {
    if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) { return true; }
    return false;
  }

  private static boolean isLetterOrNumber(final char c) {
    if (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) { return true; }
    return false;
  }

  private String trim(final String str) {
    int s = 0;
    int e = str.length() - 1;
    while ((s <= e) && (!DynatraceMetricExporter.isLetter(str.charAt(s)))) {
      s++;
    }
    while ((e >= 0) && (!DynatraceMetricExporter.isLetterOrNumber(str.charAt(e)))) {
      e--;
    }
    if (e < s) { return ""; }
    return str.substring(s, e + 1);
  }

  static final private String getIt(final JsonObject json, final String field, final String def) {
    final JsonElement elem = json.get(field);
    String result = (elem != null) ? elem.getAsString() : null;
    result = (result != null) ? result : def;
    return result;
  }

  static final private void appendDouble(final StringBuffer row, final JsonObject json, final String field) {
    final JsonElement elem = json.get(field);
    if (elem != null) {
      row.append(elem.getAsDouble());
    }
    else {
      row.append('0');
    }
  }

  @Override
  public String getId() {
    return DynatraceMetricExporter.ID;
  }

}
