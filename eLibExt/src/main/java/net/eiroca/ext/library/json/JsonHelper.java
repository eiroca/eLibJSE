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
package net.eiroca.ext.library.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonHelper {

  public static void mergeArray(final JSONObject parent, final String nodeName, final String sep) {
    final JSONArray node = (parent != null) ? parent.optJSONArray(nodeName) : null;
    if (node != null) {
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < node.length(); i++) {
        if (i > 0) {
          sb.append(sep);
        }
        sb.append(node.get(i));
      }
      parent.remove(nodeName);
      parent.put(nodeName, sb.toString());
    }
  }

  public static void replaceText(final JSONObject parent, final String nodeName, final String oldText, final String newText, final boolean removeStart) {
    String path = (parent != null) ? parent.optString(nodeName) : null;
    if (path != null) {
      path = path.replaceAll(oldText, newText);
      if (removeStart && (path.startsWith(newText))) {
        path = path.substring(newText.length());
      }
      parent.put(nodeName, path);
    }
  }

  public static void mergeObjectAsString(final JSONObject parent, final String nodeName) {
    final JSONObject param = (parent != null) ? parent.optJSONObject(nodeName) : null;
    if (param != null) {
      parent.remove(nodeName);
      parent.put(nodeName, param.toString());
    }
  }

  public static void mergeArrayAsString(final JSONObject parent, final String nodeName) {
    final JSONArray param = (parent != null) ? parent.optJSONArray(nodeName) : null;
    if (param != null) {
      parent.remove(nodeName);
      parent.put(nodeName, param.toString());
    }
  }

  public static List<String> toList(final JSONArray data) {
    if (data == null) { return null; }
    final List<String> result = new ArrayList<>();
    for (int i = 0; i < data.length(); i++) {
      result.add(data.getString(i));
    }
    return result;
  }

}
