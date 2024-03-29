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
package net.eiroca.library.sysadm.monitoring.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import net.eiroca.library.metrics.datum.IDatum;
import net.eiroca.library.sysadm.monitoring.api.DatumCheck.CheckViolation;
import net.eiroca.library.sysadm.monitoring.sdk.exporter.Exporters;

public class EventRule {

  private String id;
  private final List<EventFilter> filters = new ArrayList<>();
  private final Set<String> enabledConnectors = new HashSet<>();
  private final Set<DatumCheck> checks = new HashSet<>();

  public EventRule(final String id) {
    this.id = id;
    connectors(true);
  }

  public void addFilter(final String keyName, final String keyValue) {
    filters.add(new EventFilter(keyName, keyValue, EventFilter.Logic.OR));
  }

  public boolean apply(final SortedMap<String, Object> metadata) {
    boolean found = true;
    if ((filters != null) && (filters.size() > 0)) {
      boolean first = true;
      for (final EventFilter filter : filters) {
        boolean negate = false;
        final String keyNam = filter.keyName;
        String keyVal = filter.keyValue;
        final EventFilter.Logic logic = filter.logic;
        if (keyVal.startsWith("!")) {
          keyVal = keyVal.substring(1);
          negate = true;
        }
        final Object eventKeyVal = metadata.get(keyNam);
        boolean check;
        if ((eventKeyVal == null) || (!String.valueOf(eventKeyVal).equals(keyVal))) {
          check = negate;
        }
        else {
          check = !negate;
        }
        if (first) {
          found = check;
          first = false;
        }
        else {
          switch (logic) {
            case OR:
              found |= check;
              break;
            case AND:
              found &= check;
              break;
          }
        }
      }
    }
    return found;
  }

  public boolean export(final String connectorID) {
    return enabledConnectors.contains(connectorID);
  }

  public void connector(final String connectorID, final boolean enable) {
    if (enable) {
      enabledConnectors.add(connectorID);
    }
    else {
      enabledConnectors.remove(connectorID);
    }
  }

  public void connectors(final boolean enable) {
    enabledConnectors.clear();
    if (enable) {
      enabledConnectors.addAll(Exporters.defaultExporters);
    }
  }

  public List<EventFilter> getFilters() {
    return filters;
  }

  public void addCheck(final DatumCheck chk) {
    checks.add(chk);
  }

  public Set<DatumCheck> violations(final IDatum d) {
    if (checks.size() > 0) {
      final Set<DatumCheck> result = new HashSet<>();
      for (final DatumCheck chk : checks) {
        if (chk.check(d) != CheckViolation.OK) {
          result.add(chk);
        }
      }
      return result;
    }
    else {
      return null;
    }
  }

  @Override
  public String toString() {
    return "EventRule [filters=" + filters + ", enabledConnectors=" + enabledConnectors + ", checks=" + checks + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

}
