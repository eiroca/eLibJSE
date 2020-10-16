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
package net.eiroca.library.diagnostics.monitors;

import java.net.MalformedURLException;
import java.net.URL;
import net.eiroca.ext.library.http.utils.URLFetcher;
import net.eiroca.ext.library.http.utils.URLFetcherException;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandError;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.diagnostics.validators.GenericValidator;
import net.eiroca.library.system.IContext;

public class GenericHTTPMonitor extends ServerMonitor {

  protected URLFetcher fetcher;
  protected GenericValidator validator;

  @Override
  public void setup(final IContext context) throws CommandException {
    fetcher = new URLFetcher();
    validator = new GenericValidator();
    super.setup(context);
  }

  @Override
  public void close() throws Exception {
    Helper.close(fetcher, validator);
    validator = null;
    fetcher = null;
    super.close();
  }

  @Override
  public void readConf() throws CommandException {
    try {
      fetcher.setup(context);
    }
    catch (final URLFetcherException e) {
      final String msg = Helper.getExceptionAsString(e, false);
      context.info(msg);
      CommandException.ConfigurationError(msg);
    }
    if (validator != null) {
      validator.setup(context);
    }
  }

  @Override
  public ReturnObject fetchResponse() throws CommandException {
    final ReturnObject response = httpCall(fetcher);
    return response;
  }

  @Override
  public void parseResponse(final ReturnObject response) throws CommandException {
    mServerResult.setValue(response.getRetCode());
  }

  protected ReturnObject httpCall(final URLFetcher fetcher) {
    context.info("Executing ", fetcher.getConfig().method, " ", fetcher.getURL());
    String result = null;
    try {
      result = fetcher.execute();
    }
    catch (final URLFetcherException err) {
      final int errorCode = err.getErrorCode();
      CommandError resultError;
      final String message = String.format("Fertching error %d: %s", err.getErrorCode(), err.getMessage());
      if (errorCode < 100) {
        resultError = CommandError.Internal;
      }
      else if (errorCode < 200) {
        resultError = CommandError.Infrastructure;
      }
      else {
        resultError = CommandError.Generic;
      }
      context.warn("error:", resultError, " message: ", message);
    }
    final int httpStatus = fetcher.httpStatusCode;
    final boolean ok = (httpStatus > 0) && (httpStatus < 400);
    mServerReachable.setValue(fetcher.httpStatusCode > 0);
    mServerConnectionTimeout.setValue(fetcher.connectionTimedOut);
    mServerSocketTimeout.setValue(fetcher.socketTimedOut);
    mServerLatency.setValue(Helper.elapsed(fetcher.firstResponseStartTime, fetcher.firstResponseEndTime));
    mServerResponseTime.setValue(Helper.elapsed(fetcher.responseStartTime, fetcher.responseEndTime));
    mServerStatus.setValue(!ok);
    final ReturnObject summary = new ReturnObject(httpStatus, result);
    return summary;
  }

  protected URL getURL(final String confEntry, final String host) throws CommandException {
    final String URLstring = context.getConfigString(confEntry, null);
    URL url = null;
    try {
      url = new URL(formatURL(URLstring, host));
    }
    catch (final MalformedURLException e) {
      CommandException.ConfigurationError("Invalid " + URLstring + " -> " + e.getMessage());
    }
    return url;
  }

  protected String formatURL(final String urlFormat, final String host) throws CommandException {
    if (LibStr.isEmptyOrNull(urlFormat)) {
      CommandException.ConfigurationError("Invalid " + urlFormat);
    }
    final String url = urlFormat.replaceAll("\\{host\\}", host);
    return url;
  }

}
