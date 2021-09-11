/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.diagnostics.monitors;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONObject;
import net.eiroca.ext.library.http.utils.URLFetcherConfig;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.ReturnObject;

public class RESTServerMonitor extends GenericHTTPMonitor {

  public RESTServerMonitor() {
    super();
  }

  @Override
  public boolean preCheck(final InetAddress host) throws CommandException {
    URL url;
    try {
      url = getURL(host);
    }
    catch (final MalformedURLException e) {
      url = null;
    }
    fetcher.setURL(url);
    fetcher.setMethod(URLFetcherConfig.METHOD_GET, null);
    return (url != null);
  }

  @Override
  public void parseResponse(final ReturnObject response) throws CommandException {
    context.debug("Parsing JSON");
    try {
      final JSONObject obj = new JSONObject(response.getOutput());
      parseJSON(obj);
    }
    catch (final Exception ex) {
      context.info("Error parsing JSON error: ", ex.toString());
      CommandException.Invalid("Parsing JSON response failed");
    }
  }

  public URL getURL(final InetAddress host) throws MalformedURLException {
    return null;
  }

  public void parseJSON(final JSONObject obj) {
  }

}
