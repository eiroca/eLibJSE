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
package net.eiroca.library.data;

public class Status {

  public static final Status OK = new Status(0, "OK");

  private final int code;
  private final String message;

  public Status(final int code, final String message) {
    this.code = code;
    this.message = message;
  }

  public Status(final Status s) {
    code = s.code;
    message = s.message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int hashCode() {
    return code;
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Status)) { return false; }
    final Status os = (Status)o;
    return getCode() == os.getCode();
  }

  @Override
  public String toString() {
    return "Status [code=" + code + ", message='" + message + "']";
  }

}
