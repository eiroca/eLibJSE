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
package net.eiroca.ext.library.gson;

import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.data.Tags;

public class LibGson {

  public interface NodeVisitor {

    void visit(String predfix, String name, String val);

  }

  public static final Gson builder = new Gson();

  final public static JsonObject fromString(String data) {
    try {
      return JsonParser.parseString(data).getAsJsonObject();
    }
    catch (Exception e) {
      return null;
    }
  }

  final public static void visit(String prefix, JsonObject gson, boolean nested, boolean exportNull, boolean exportArray, String sep, NodeVisitor delegate) {
    for (Map.Entry<String, JsonElement> entry : gson.entrySet()) {
      String name = entry.getKey();
      JsonElement node = entry.getValue();
      if (node.isJsonObject()) {
        if (nested) {
          visit(prefix + name + sep, node.getAsJsonObject(), nested, exportNull, exportArray, sep, delegate);
        }
      }
      else if (node.isJsonArray()) {
        if (exportArray) delegate.visit(prefix, name, node.toString());
      }
      else if (node.isJsonNull()) {
        if (exportNull) delegate.visit(prefix, name, null);
      }
      else {
        delegate.visit(prefix, name, node.getAsString());
      }
    }
  }

  final public static void visit(JsonObject gson, boolean exportNull, boolean exportArray, String sep, NodeVisitor delegate) {
    visit("", gson, true, exportNull, exportArray, sep, delegate);
  }

  final public static Tags getTags(String data, boolean expandNodes, String missingPrefix, Map<String, String> mappedName) {
    if (data == null) { return null; }
    JsonObject gson = LibGson.fromString(data);
    if (gson == null) { return null; }
    Tags result = new Tags();
    LibGson.visit("", gson, expandNodes, false, false, ".",
        (prefix, nodeName, value) -> {
          String name = prefix + nodeName;
          String newName = null;
          final boolean exist = (mappedName != null) && mappedName.containsKey(name);
          if (!exist) {
            if ((missingPrefix != null)) {
              newName = missingPrefix + name;
              if (mappedName != null) {
                mappedName.put(name, newName);
              }
            }
          }
          else {
            newName = mappedName.get(name);
          }
          if (LibStr.isNotEmptyOrNull(newName)) {
            result.add(newName, value);
          }

        });
    return result;
  }

}
