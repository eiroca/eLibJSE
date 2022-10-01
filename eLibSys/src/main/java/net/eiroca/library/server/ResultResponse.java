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
package net.eiroca.library.server;

public class ResultResponse<T> extends ServerResponse {

  private T result;

  public ResultResponse(final int status) {
    this(status, null, null);
  }

  public ResultResponse(final int status, final String message) {
    this(status, message, null);
  }

  public ResultResponse(final int status, final String message, final T result) {
    super(status, message);
    this.result = result;
  }

  public T getResult() {
    return result;
  }

  public void setResult(final T result) {
    this.result = result;
  }

  public Object getPayload() {
    return result;
  }

}
