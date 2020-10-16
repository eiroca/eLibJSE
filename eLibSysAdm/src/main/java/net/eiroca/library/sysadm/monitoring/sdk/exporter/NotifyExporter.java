/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.impl.client.CloseableHttpClient;
import net.eiroca.ext.library.gson.GsonCursor;
import net.eiroca.ext.library.gson.SimpleGson;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.sysadm.monitoring.api.EventRule;
import net.eiroca.library.system.IContext;

public class NotifyExporter extends GenericExporter {

  public static String ID = "notify";
  //
  public static StringParameter _notifyUrl = new StringParameter(NotifyExporter.config, "notifyUrl", null);
  // Dynamic mapped to parameters
  protected String config_notifyUrl;
  //
  private static final Pattern regExParams = Pattern.compile("\\$\\{(.+?)\\}");

  public NotifyExporter() {
    super();
  }

  @Override
  public void setup(final IContext context) throws Exception {
    super.setup(context);
    GenericExporter.config.convert(context, GenericExporter.CONFIG_PREFIX, this, "config_");
    context.info(this.getClass().getName(), " setup done");
  }

  private CloseableHttpClient client;

  @Override
  public boolean beginBulk() {
    if (config_notifyUrl != null) {
      client = HttpClientHelper.getHttpClient(null);
    }
    return (config_notifyUrl != null) && (client != null);
  }

  @Override
  public void process(final Event event) {
    final EventRule rule = event.getRule();
    if (rule == null) { return; }
    final String url = expand(event, config_notifyUrl);
    final String r = HttpClientHelper.GET(client, url);
    context.info(url + " --> " + r);
  }

  @Override
  public void endBulk() {
    client = null;
  }

  public String expand(final Event event, final String s) {
    final SimpleGson data = event.getData();
    final GsonCursor json = new GsonCursor(data);
    final Matcher m = NotifyExporter.regExParams.matcher(s);
    String result = s;
    while (m.find()) {
      final String p = m.group(1);
      String v = json.getString(p);
      if (v == null) {
        v = "";
      }
      else {
        v = LibStr.urlEncode(v);
      }
      result = result.replaceFirst(NotifyExporter.regExParams.pattern(), v);
    }
    return result;
  }

  @Override
  public String getId() {
    return NotifyExporter.ID;
  }

}
