/**
 *
 * Copyright (C) 1999-2019 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.diagnostics.util;

public class ReturnObject {

  private Integer retCode = null;
  private String output = null;

  public ReturnObject() {
    retCode = 0;
    output = null;
  }

  public ReturnObject(final int retCode, final String output) {
    this.retCode = retCode;
    this.output = output;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(final String output) {
    this.output = output;
  }

  public Integer getRetCode() {
    return retCode;
  }

  public void setRetCode(final Integer rc) {
    retCode = rc;
  }

}
