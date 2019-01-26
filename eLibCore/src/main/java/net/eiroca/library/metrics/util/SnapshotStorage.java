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
package net.eiroca.library.metrics.util;

import java.util.HashMap;
import java.util.UUID;

public class SnapshotStorage {

  private static HashMap<UUID, MeasureSnapshot> store = new HashMap<>();

  public static MeasureSnapshot put(UUID id, MeasureSnapshot snap) {
    synchronized (store) {
      return store.put(id, snap);
    }
  }

  public static MeasureSnapshot get(UUID id) {
    synchronized (store) {
      return store.get(id);
    }
  }

}
