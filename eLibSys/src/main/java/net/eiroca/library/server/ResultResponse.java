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
package net.eiroca.library.server;

public class ResultResponse extends ServerResponse {

  Object result;

  public ResultResponse(final int status) {
    this(status, null, null);
  }

  public ResultResponse(final int status, final String message, final Object result) {
    super(status, message);
    this.result = result;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(final Object result) {
    this.result = result;
  }
}
