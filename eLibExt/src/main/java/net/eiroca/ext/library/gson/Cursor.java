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

import com.google.gson.JsonObject;

public class Cursor {

  private final SimpleJson simpleJson;
  public String propertyName;
  public String parent;
  public JsonObject node;

  public Cursor(final SimpleJson simpleJson) {
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

}
