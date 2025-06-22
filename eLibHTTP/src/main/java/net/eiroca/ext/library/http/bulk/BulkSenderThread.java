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
package net.eiroca.ext.library.http.bulk;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import net.eiroca.ext.library.http.HttpClientHelper;
import net.eiroca.library.core.Helper;
import net.eiroca.library.system.Logs;

public class BulkSenderThread implements Runnable {

  private static final Logger logger = Logs.getLogger();

  private CloseableHttpClient httpclient;
  private final HttpEntity entity;
  private final BulkSender<?> owner;
  private final int numEvents;
  private final HttpContext context;

  public BulkSenderThread(final BulkSender<?> bulkSender, final HttpEntity entity, final int numEvents) {
    this.entity = entity;
    this.numEvents = numEvents;
    owner = bulkSender;
    context = new BasicHttpContext();
  }

  @Override
  public void run() {
    BulkSenderThread.logger.debug("Running...");
    final String serverURL = owner.getServerURL();
    final String acceptType = owner.getAcceptType();
    final String auth = owner.getAuthorization();
    final HttpPost httpRequest = new HttpPost(serverURL);
    httpRequest.setEntity(entity);
    httpRequest.setHeader(entity.getContentType());
    final Header encoding = entity.getContentEncoding();
    if (acceptType != null) {
      httpRequest.setHeader(HttpHeaders.ACCEPT, acceptType);
    }
    if (auth != null) {
      httpRequest.setHeader(HttpHeaders.AUTHORIZATION, auth);
    }
    if (encoding != null) {
      httpRequest.setHeader(encoding);
    }
    final long sendStartTime = System.currentTimeMillis();
    try {
      BulkSenderThread.logger.trace("exceuting... {}", numEvents);
      final CloseableHttpClient client = getHttpClient();
      final CloseableHttpResponse response = client.execute(httpRequest, context);
      final int responseCode = response.getStatusLine().getStatusCode();
      long elapsed = System.currentTimeMillis() - sendStartTime;
      if (elapsed < 1) {
        elapsed = 1;
      }
      final boolean overload = owner.checkOverload(responseCode);
      final boolean failed = owner.checkFailed(responseCode);
      boolean valid = true;
      owner.setOverload(overload);
      owner.stats.incEventSent(numEvents);
      owner.stats.addSendTime(elapsed);
      final long checkStartTime = System.currentTimeMillis();
      try {
        final HttpEntity entity2 = response.getEntity();
        valid = owner.checkValidResponse(responseCode, entity2, numEvents);
      }
      finally {
        Helper.close(response);
        httpRequest.releaseConnection();
        if (failed || !valid) {
          BulkSenderThread.logger.warn(numEvents + "\t" + httpRequest + "\t" + responseCode + "\t" + Math.round((numEvents / (elapsed * .001))));
        }
        else {
          BulkSenderThread.logger.debug(numEvents + "\t" + httpRequest + "\t" + responseCode + "\t" + Math.round((numEvents / (elapsed * .001))));
        }

      }
      owner.stats.addCheckTime(System.currentTimeMillis() - checkStartTime);
      BulkSenderThread.logger.debug("Exiting... {}", responseCode);
    }
    catch (final IOException e) {
      BulkSenderThread.logger.warn("Sending error " + e.getMessage(), e);
      Helper.close(httpclient);
      httpclient = null;
      httpclient = getHttpClient();
    }
  }

  public CloseableHttpClient getHttpClient() {
    if (httpclient == null) {
      try {
        httpclient = HttpClientHelper.createAcceptAllClient(null);
      }
      catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
        httpclient = HttpClients.createDefault();
      }
    }
    return httpclient;
  }

}
