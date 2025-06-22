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

public class BulkEntry {

  public String _id;
  public int status = 0;

  public String meta;
  public String data;

  public BulkEntry() {
  }

  public BulkEntry(final String data, final String meta) {
    super();
    this.data = data;
    this.meta = meta;
  }

  public BulkEntry(final String data) {
    this(data, null);
  }

  public int entrySize() {
    int size = data.length() + 1;
    if (meta != null) {
      size += meta.length() + 1;
    }
    return size;
  }

  public String bulkEntry() {
    final StringBuffer sb = new StringBuffer();
    if (meta != null) {
      sb.append(meta).append('\n');
    }
    sb.append(data).append('\n');
    return sb.toString();
  }

}
