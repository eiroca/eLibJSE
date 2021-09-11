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
package net.eiroca.ext.library.http.utils;

public class URLFetcherException extends Exception {

  private static final long serialVersionUID = -8290290786607592964L;
  int errorCode;

  public int getErrorCode() {
    return errorCode;
  }

  public URLFetcherException(final int errCode, final String message) {
    super(message);
    errorCode = errCode;
  }

  @Override
  public String toString() {
    return errorCode + ": " + super.toString();
  }

  public static final URLFetcherException InvalidHost(final String host) throws URLFetcherException {
    throw new URLFetcherException(10, host + " is an invalid host");
  }

  public static final URLFetcherException InvalidMethod(final String method) throws URLFetcherException {
    throw new URLFetcherException(11, method + " is an invalid method");
  }

  public static final URLFetcherException InvalidParameters(final Exception ex) throws URLFetcherException {
    throw new URLFetcherException(12, "Setting HTTP client parameters failed for " + ex.getMessage());
  }

  public static final URLFetcherException URLFetchFailed(final Exception ex) throws URLFetcherException {
    throw new URLFetcherException(101, "Unable to read form URL: " + ex.getMessage());
  }
}
