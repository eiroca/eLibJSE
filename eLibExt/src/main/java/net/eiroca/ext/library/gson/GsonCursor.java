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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eiroca.library.core.LibStr;

public class GsonCursor {

  private static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  private final SimpleGson simpleJson;
  public String propertyName;
  public String parent;
  public JsonObject node;

  public GsonCursor(final SimpleGson simpleJson) {
    this.simpleJson = simpleJson;
  }

  public void seek(final String name) {
    if (!simpleJson.expandName) {
      parent = null;
      propertyName = name;
      node = simpleJson.root;
    }
    else {
      final int pos = name.lastIndexOf('.');
      if (pos > 0) {
        parent = name.substring(0, pos);
        propertyName = name.substring(pos + 1);
        node = simpleJson.getNode(simpleJson.root, parent);
      }
      else {
        parent = null;
        propertyName = name;
        node = simpleJson.root;
      }
    }
  }

  public void addJson(final String name, final String value) {
    seek(name);
    node.add(propertyName, JsonParser.parseString(value));
  }

  public void addProperty(final String name, final String val) {
    seek(name);
    node.addProperty(propertyName, val);
  }

  public void addProperty(final String name, final double value) {
    seek(name);
    node.addProperty(propertyName, value);
  }

  public void addProperty(final String name, final boolean value) {
    seek(name);
    node.addProperty(propertyName, value);
  }

  public void addProperty(final String name, final String[] value) {
    seek(name);
    final JsonArray e = new JsonArray();
    for (final String s : value) {
      e.add(s);
    }
    node.add(propertyName, e);
  }

  public void addProperty(final String name, final List<?> value) {
    seek(name);
    final JsonArray e = new JsonArray();
    for (final Object s : value) {
      e.add(s != null ? String.valueOf(s) : null);
    }
    node.add(propertyName, e);
  }

  public void addProperty(final String name, final Date value, final SimpleDateFormat sdf) {
    seek(name);
    node.addProperty(propertyName, sdf.format(value));
  }

  public void addProperty(final String name, final Date value) {
    seek(name);
    node.addProperty(propertyName, GsonCursor.ISO8601.format(value));
  }

  public void addProperty(final String name, final JsonElement value) {
    seek(name);
    node.add(propertyName, value);
  }

  public String getString(final String name) {
    seek(name);
    if (node == null) { return null; }
    final JsonElement e = node.get(propertyName);
    if (e == null) { return null; }
    return e.getAsString();
  }

  public void set(final String name, final double val) {
    addProperty(name, val);
  }

  public void set(final String name, final String val) {
    if (LibStr.isNotEmptyOrNull(val)) {
      addProperty(name, val);
    }
  }

  public void set(final String name, final List<String> val) {
    if ((val != null) && (val.size() > 0)) {
      addProperty(name, val);
    }
  }

  public void set(final String name, final Date val, final SimpleDateFormat sdf) {
    if (val != null) {
      addProperty(name, sdf.format(val));
    }
  }

  public void set(final String name, final boolean val) {
    addProperty(name, val);
  }

  @Override
  public String toString() {
    return simpleJson != null ? simpleJson.toString() : "";
  }

}
