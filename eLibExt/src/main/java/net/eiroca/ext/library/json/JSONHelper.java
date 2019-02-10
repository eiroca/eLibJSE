/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
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

import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;

public class JSONHelper {

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

  public static String getString(final JsonObject json, final String name) {
    String result = null;
    final JsonElement e = json.get(name);
    if (e != null) {
      final String data = e.getAsString();
      result = LibStr.isEmptyOrNull(data) ? null : data;
    }
    return result;
  }

  public static int getInt(final JsonObject json, final String name, int defVal) {
    final JsonElement e = json.get(name);
    if (e != null) {
      final String data = e.getAsString();
      return Helper.getInt(data, defVal);
    }
    return defVal;
  }

}
