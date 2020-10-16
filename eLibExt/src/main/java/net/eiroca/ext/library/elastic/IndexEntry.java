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
package net.eiroca.ext.library.elastic;

public class IndexEntry {

  public int status = 0;
  public String _id;
  public String meta;
  public String data;

  public IndexEntry() {
  }

  public IndexEntry(final String index, final String type, final String id, final String pipeline, final String document, int version) {
    super();
    _id = id;
    meta = getMetadata(index, type, id, pipeline, version);
    data = document;
  }

  private String getMetadata(final String index, final String type, final String id, final String pipeline, int version) {
    final StringBuilder sb = new StringBuilder();
    sb.append("{\"index\":{\"_index\":\"").append(index).append("\"");
    if ((type != null) && (version < 7)) {
      sb.append(",\"_type\":\"").append(type).append("\"");
    }
    if (id != null) {
      sb.append(",\"_id\":\"").append(id).append("\"");
    }
    if (pipeline != null) {
      sb.append(",\"pipeline\":\"").append(pipeline).append("\"");
    }
    sb.append("}}");
    return sb.toString();
  }

  public int entrySize() {
    return meta.length() + data.length() + 2;
  }

  public String bulkEntry() {
    final String entry = meta + "\n" + data + "\n";
    return entry;
  }

}
