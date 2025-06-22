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
package net.eiroca.ext.library.gson;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SimpleGson {

  private static final Gson builder = new Gson();

  private final Map<String, JsonObject> treeCache = new HashMap<>();
  final JsonObject root;
  final boolean expandName;

  public SimpleGson(final boolean expandName) {
    this.expandName = expandName;
    root = new JsonObject();
  }

  public SimpleGson(final String data) {
    this.expandName = true;
    root = JsonParser.parseString(data).getAsJsonObject();
  }

  @Override
  public String toString() {
    return SimpleGson.builder.toJson(root);
  }

  public JsonObject getRoot() {
    return root;
  }

  public String getPropertyName(final String newName) {
    if (!expandName) { return newName; }
    final int pos = newName.lastIndexOf('.');
    return pos > 0 ? newName.substring(pos + 1) : newName;
  }

  public String getParent(final String newName) {
    if (!expandName) { return null; }
    final int pos = newName.lastIndexOf('.');
    return pos > 0 ? newName.substring(0, pos) : null;
  }

  public String get(final String name, String def) {
    JsonObject node = getNode(name);
    return (node != null) ? node.getAsString() : def;
  }

  public boolean getBoolean(final String name, boolean def) {
    JsonObject node = getNode(name);
    try {
      return (node != null) ? node.getAsBoolean() : def;
    }
    catch (ClassCastException e) {
      return def;
    }
  }

  public JsonObject getNode(final String name) {
    JsonObject node;
    if (expandName) {
      final String parent = getParent(name);
      node = (parent == null) ? root : getNode(root, parent);
    }
    else {
      node = root;
    }
    return node;
  }

  public JsonObject getNode(final JsonObject e, final String nodeName) {
    JsonObject node = treeCache.get(nodeName);
    if (node == null) {
      final String parent = getParent(nodeName);
      final JsonObject parentNode = (parent == null) ? e : getNode(e, parent);
      node = new JsonObject();
      parentNode.add(getPropertyName(nodeName), node);
      treeCache.put(nodeName, node);
    }
    return node;
  }

}
