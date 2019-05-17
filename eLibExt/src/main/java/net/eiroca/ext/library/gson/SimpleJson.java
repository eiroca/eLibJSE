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
package net.eiroca.ext.library.gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eiroca.library.core.LibStr;

public class SimpleJson {

  private static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  private static final Gson builder = new Gson();
  private static final JsonParser parser = new JsonParser();

  private final Map<String, JsonObject> treeCache = new HashMap<>();
  final JsonObject root;
  final boolean expandName;

  public SimpleJson(final boolean expandName) {
    this.expandName = expandName;
    root = new JsonObject();
  }

  @Override
  public String toString() {
    return SimpleJson.builder.toJson(root);
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

  public void addJson(final Cursor c, final String name, final String value) {
    c.seek(this, name);
    c.node.add(c.propertyName, SimpleJson.parser.parse(value));
  }

  public void addProperty(final Cursor c, final String name, final String val) {
    c.seek(this, name);
    c.node.addProperty(c.propertyName, val);
  }

  public void addProperty(final Cursor c, final String name, final double value) {
    c.seek(this, name);
    c.node.addProperty(c.propertyName, value);
  }

  public void addProperty(final Cursor c, final String name, final boolean value) {
    c.seek(this, name);
    c.node.addProperty(c.propertyName, value);
  }

  public void addProperty(final Cursor c, final String name, final String[] value) {
    c.seek(this, name);
    final JsonArray e = new JsonArray();
    for (final String s : value) {
      e.add(s);
    }
    c.node.add(c.propertyName, e);
  }

  public void addProperty(final Cursor c, final String name, final List<?> value) {
    c.seek(this, name);
    final JsonArray e = new JsonArray();
    for (final Object s : value) {
      e.add(s != null ? String.valueOf(s) : null);
    }
    c.node.add(c.propertyName, e);
  }

  public void addProperty(final Cursor c, final String name, final Date value, final SimpleDateFormat sdf) {
    c.seek(this, name);
    c.node.addProperty(c.propertyName, sdf.format(value));
  }

  public void addProperty(final Cursor c, final String name, final Date value) {
    c.seek(this, name);
    c.node.addProperty(c.propertyName, SimpleJson.ISO8601.format(value));
  }

  public void addProperty(final Cursor c, final String name, final JsonElement value) {
    c.seek(this, name);
    c.node.add(c.propertyName, value);
  }

  public String getString(final Cursor c, final String name) {
    c.seek(this, name);
    if (c.node == null) { return null; }
    final JsonElement e = c.node.get(c.propertyName);
    if (e == null) { return null; }
    return e.getAsString();
  }

  public void set(final Cursor c, final String name, final double val) {
    addProperty(c, name, val);
  }

  public void set(final Cursor c, final String name, final String val) {
    if (LibStr.isNotEmptyOrNull(val)) {
      addProperty(c, name, val);
    }
  }

  public void set(final Cursor c, final String name, final List<String> val) {
    if ((val != null) && (val.size() > 0)) {
      addProperty(c, name, val);
    }
  }

  public void set(final Cursor c, final String name, final Date val, final SimpleDateFormat sdf) {
    if (val != null) {
      addProperty(c, name, sdf.format(val));
    }
  }

  public void set(final Cursor c, final String name, final boolean val) {
    addProperty(c, name, val);
  }

}
