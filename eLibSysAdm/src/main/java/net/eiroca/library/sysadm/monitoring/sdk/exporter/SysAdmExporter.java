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

import java.util.ArrayList;
import java.util.Collection;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import net.eiroca.ext.library.gson.SimpleGson;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.sysadm.monitoring.api.Event;
import net.eiroca.library.system.IContext;

public class SysAdmExporter extends GenericExporter {

  private static final String ESYSADM_TOKEN_HEADER = "X-eSysAdm-TOKEN";

  public static final String ID = "eSysAdm".toLowerCase();
  //
  public static StringParameter _eSysAdmUrl = new StringParameter(SysAdmExporter.config, "eSysAdmUrl", null);
  public static StringParameter _eSysAdmToken = new StringParameter(SysAdmExporter.config, "eSysAdmToken", null);
  // Dynamic mapped to parameters
  protected String config_eSysAdmUrl;
  protected String config_eSysAdmToken;
  //
  private CloseableHttpClient client;

  public SysAdmExporter() {
    super();
  }

  @Override
  public void setup(final IContext context) throws Exception {
    super.setup(context);
    GenericExporter.config.convert(context, GenericExporter.CONFIG_PREFIX, this, "config_");
    final String token = config_eSysAdmToken != null ? config_eSysAdmToken.substring(0, 8) : null;
    context.info(this.getClass().getName(), " setup done, url=", config_eSysAdmUrl, " token=", token);
  }

  @Override
  public boolean beginBulk() {
    if (config_eSysAdmUrl != null) {
      Collection<Header> headers = null;
      if (config_eSysAdmToken != null) {
        headers = new ArrayList<>();
        headers.add(new BasicHeader(SysAdmExporter.ESYSADM_TOKEN_HEADER, config_eSysAdmToken));
      }
      final HttpHost proxy = null;
      client = HttpClientHelper.getHttpClient(proxy, headers);
    }
    final boolean result = (config_eSysAdmUrl != null) && (client != null);
    GenericExporter.logger.debug("beginbulk()=" + result);
    return result;
  }

  @Override
  public void process(final Event event) {
    GenericExporter.logger.debug("process()");
    final SimpleGson json = event.getData();
    final String _doc = json.toString();
    final String url = config_eSysAdmUrl;
    final String r = HttpClientHelper.POST(client, url, _doc, ContentType.APPLICATION_JSON);
    context.debug("POST " + url + " " + _doc + " --> " + r);
  }

  @Override
  public void endBulk() {
    client = null;
  }

  @Override
  public String getId() {
    return SysAdmExporter.ID;
  }

}
