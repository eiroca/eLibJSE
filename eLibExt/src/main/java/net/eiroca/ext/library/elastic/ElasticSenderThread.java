/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.ext.library.elastic;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import net.eiroca.library.system.Logs;

public class ElasticSenderThread implements Runnable {

  private static final Logger logger = Logs.getLogger();

  private static final String HEADER_CONTENT_TYPE = "Content-type";
  private static final String HEADER_ACCEPT = "Accept";
  private static final String STR_CHARSET = ";charset=";
  private static final String STR_APPLICATIONJSON = "application/json";
  private static final String STR_BULKMIMETYPE = "application/json";

  CloseableHttpClient httpclient;
  HttpEntity entity;
  ElasticBulk owner;
  int numEvents;
  private final HttpContext context;

  public ElasticSenderThread(final ElasticBulk elasticBulk, final HttpEntity entity, final int numEvents) {
    this.entity = entity;
    this.numEvents = numEvents;
    owner = elasticBulk;
    context = new BasicHttpContext();
  }

  @Override
  public void run() {
    ElasticSenderThread.logger.debug("Running...");
    final HttpPut httpPut = new HttpPut(owner.elasticServer);
    httpPut.setEntity(entity);
    httpPut.setHeader(ElasticSenderThread.HEADER_ACCEPT, ElasticSenderThread.STR_APPLICATIONJSON);
    httpPut.setHeader(ElasticSenderThread.HEADER_CONTENT_TYPE, ElasticSenderThread.STR_BULKMIMETYPE + ElasticSenderThread.STR_CHARSET + entity.getContentEncoding());
    final long sendStartTime = System.currentTimeMillis();
    try {
      ElasticSenderThread.logger.trace("exceuting... {}", numEvents);
      final CloseableHttpResponse response = getHttpClient().execute(httpPut, context);
      final int responseCode = response.getStatusLine().getStatusCode();
      long elapsed = System.currentTimeMillis() - sendStartTime;
      if (elapsed < 1) {
        elapsed = 1;
      }
      owner.stats.incEventSent(numEvents);
      owner.stats.addSendTime(elapsed);
      ElasticSenderThread.logger.debug(numEvents + "\t" + httpPut + "\t" + responseCode + "\t" + Math.round((numEvents / (elapsed * .001))));
      final long checkStartTime = System.currentTimeMillis();
      try {
        final HttpEntity entity2 = response.getEntity();
        owner.checkResult(responseCode, entity2, numEvents);
      }
      finally {
        response.close();
      }
      owner.stats.addCheckTime(System.currentTimeMillis() - checkStartTime);
      ElasticSenderThread.logger.debug("Exiting... {}", responseCode);
    }
    catch (final ClientProtocolException e) {
      ElasticSenderThread.logger.warn("Sending error " + e.getMessage(), e);
    }
    catch (final IOException e) {
      ElasticSenderThread.logger.warn("Sending error " + e.getMessage(), e);
    }
  }

  public CloseableHttpClient getHttpClient() {
    if (httpclient == null) {
      httpclient = HttpClients.createDefault();
    }
    return httpclient;
  }

}
