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
package net.eiroca.library.sysadm.monitoring.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import net.eiroca.library.sysadm.monitoring.sdk.connector.ElasticConnector;
import net.eiroca.library.sysadm.monitoring.sdk.connector.LoggerConnector;
import java.util.Set;
import java.util.SortedMap;

public class EventRule {

  private Map<String, String> filter = new HashMap<>();
  private Set<String> enabledConnectors = new HashSet<>();

  public EventRule() {
    connectors(true);
  }

  public void addFilter(String keyName, String keyValue) {
    filter.put(keyName, keyValue);
  }

  public boolean apply(SortedMap<String, Object> metadata) {
    boolean found = true;
    if ((filter != null) && (filter.size() > 0)) {
      for (Entry<String, String> filter : filter.entrySet()) {
        String keyNam = filter.getKey();
        String keyVal = filter.getValue();
        Object eventKeyVal = metadata.get(keyNam);
        if ((eventKeyVal == null) || (!String.valueOf(eventKeyVal).equals(keyVal))) {
          found = false;
          break;
        }
      }
    }
    return found;
  }

  public boolean export(String connectorID) {
    return enabledConnectors.contains(connectorID);
  }

  public void connector(String connectorID, boolean enable) {
    if (enable) enabledConnectors.add(connectorID);
    else enabledConnectors.remove(connectorID);
  }

  public void connectors(boolean enable) {
    enabledConnectors.clear();
    if (enable) {
      enabledConnectors.add(ElasticConnector.ID);
      enabledConnectors.add(LoggerConnector.ID);
    }
  }

}
