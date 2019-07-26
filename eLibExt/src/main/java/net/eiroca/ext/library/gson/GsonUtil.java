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
package net.eiroca.ext.library.gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;

public class GsonUtil {

  final public static String toJSON(final Object obj) {
    return new Gson().toJson(obj);
  }

  final public static void compatta(final JsonObject o, final String key) {
    final JsonElement e = o.get(key);
    if (e == null) { return; }
    final StringBuffer sb = new StringBuffer();
    if (e.isJsonObject()) {
      final JsonObject target = o.getAsJsonObject(key);
      for (final Map.Entry<String, JsonElement> entry : target.entrySet()) {
        final String name = entry.getKey();
        final String value = entry.getValue().toString();
        sb.append(name).append(" = ").append(value).append("\n");
      }
    }
    else if (e.isJsonArray()) {
      final JsonArray target = o.getAsJsonArray(key);
      for (final JsonElement entry : target) {
        sb.append(entry.toString()).append("\n");
      }
    }
    else {
      sb.append(e.getAsString());
    }
    o.remove(key);
    o.addProperty(key, sb.toString());
  }

  final public static void pulisci(final JsonObject o) {
    final Map<String, JsonElement> changes = new HashMap<>();
    for (final Map.Entry<String, JsonElement> entry : o.entrySet()) {
      final String name = entry.getKey();
      String newName = null;
      if (LibStr.isEmptyOrNull(name)) {
        newName = "default";
      }
      else if (name.contains(".")) {
        newName = name.replace('.', '_');
      }
      if (newName != null) {
        changes.put(name, null);
        changes.put(newName, entry.getValue());
      }
    }
    for (final Map.Entry<String, JsonElement> entry : changes.entrySet()) {
      final String name = entry.getKey();
      final JsonElement value = entry.getValue();
      if (value == null) {
        o.remove(name);
      }
      else {
        o.add(name, value);
      }
    }
  }

  public static void moveUp(final JsonObject jobject, final String key) {
    final JsonElement node = jobject.get(key);
    if ((node != null) && (node.isJsonObject())) {
      for (final Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
        final String name = entry.getKey();
        final JsonElement value = entry.getValue();
        jobject.add(name, value);
      }
      jobject.remove(key);
    }

  }

  final public static void decodeStack(final JsonObject jobject, final String key, final String errorKey) {
    final JsonElement stack = jobject.get(key);
    if (stack == null) { return; }
    final String stackTrace = stack.getAsString();
    final int endPos = stackTrace.indexOf("\n");
    String error;
    if (endPos > 0) {
      error = stackTrace.substring(0, endPos);
    }
    else {
      error = stackTrace;
    }
    jobject.addProperty(errorKey, error);
  }

  final public static void compatta_array(final JsonObject o, final String key) {
    final JsonElement e = o.get(key);
    if (e == null) { return; }
    final JsonObject target = e.getAsJsonObject();
    for (int i = 0; i < 9; i++) {
      final JsonObject elem = target.getAsJsonObject("" + i);
      if (elem == null) {
        break;
      }
      final StringBuffer sb = new StringBuffer();
      for (final Map.Entry<String, JsonElement> entry : elem.entrySet()) {
        final String name = entry.getKey();
        final String value = entry.getValue().toString();
        sb.append(name).append(" = ").append(value).append("\n");
      }
      o.addProperty(key + "_" + i, sb.toString());
    }
    o.remove(key);
  }

  public static String getString(final JsonObject o, final String name) {
    String result = null;
    final JsonElement e = o.get(name);
    if (e != null) {
      final String data = e.getAsString();
      result = LibStr.isEmptyOrNull(data) ? null : data;
    }
    return result;
  }

  public static int getInt(final JsonObject json, final String name, final int defVal) {
    final JsonElement e = json.get(name);
    if (e != null) {
      final String data = e.getAsString();
      return Helper.getInt(data, defVal);
    }
    return defVal;
  }

  public static Date getDate(final JsonObject o, final String name, final SimpleDateFormat... formats) {
    final JsonElement dataFld = o.get(name);
    final String data = (dataFld != null) ? dataFld.getAsString() : null;
    Date result = null;
    if (data != null) {
      for (final SimpleDateFormat fmt : formats) {
        try {
          result = fmt.parse(data);
          break;
        }
        catch (final ParseException e) {
        }
      }
    }
    return result;
  }

  public static String getLower(final JsonObject o, final String name) {
    final JsonElement e = (name != null) ? o.get(name) : null;
    final String data = (e != null) ? (!e.isJsonNull() ? e.getAsString() : null) : null;
    return LibStr.isEmptyOrNull(data) ? null : data.toLowerCase();
  }

  public static void move(final JsonObject dest, final String destName, final JsonObject source, final String srcName) {
    final String val = GsonUtil.getString(source, srcName);
    if (val == null) { return; }
    dest.addProperty(destName, val);
    source.remove(srcName);
  }

}
