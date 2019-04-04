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
package net.eiroca.library.sysadm.monitoring.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.sysadm.monitoring.api.EventRule;

public class RuleEngine {

  private static final char HASH_SEPARATOR = '\t';
  private Set<String> hashKeys = new HashSet<>();
  private List<EventRule> ruleSet = new ArrayList<>();
  private Map<String, EventRule> cache = new HashMap<>();

  public void loadRules(Properties config) {
    Map<String, EventRule> alias = new HashMap<>();
    for (String name : config.stringPropertyNames()) {
      String val = config.getProperty(name);
      String[] command = name.split("\\.", -1);
      String param = null;
      if (command.length < 2) continue;
      if (command.length == 3) param = command[2];
      String ruleName = command[0];
      EventRule rule = alias.get(ruleName);
      if (rule == null) {
        rule = new EventRule();
        alias.put(ruleName, rule);
      }
      String ruleType = command[1];
      switch (ruleType) {
        case "filter":
          if (param != null) {
            rule.addFilter(param, val);
          }
          break;
        case "export":
          boolean enable = true;
          if (!LibStr.isEmptyOrNull(val)) {
            val = val.toLowerCase();
            if (val.startsWith("f")) enable = false;
            else if (val.equals("off")) enable = false;
            else if (val.equals("disabled")) enable = false;
            else if (val.equals("0")) enable = false;
          }
          if ((param == null) || ("*".equals(param)) || ("all".equals(param.toLowerCase()))) {
            rule.connectors(enable);
          }
          else {
            rule.connector(param, enable);
          }
          break;
      }
    }
  }

  public void addRule(EventRule rule) {
    ruleSet.add(rule);
  }

  public EventRule ruleFor(SortedMap<String, Object> metadata) {
    String hash = getHash(metadata);
    EventRule result = getCached(hash, metadata);
    return result;

  }

  private EventRule findRule(SortedMap<String, Object> metadata) {
    for (EventRule rule : ruleSet) {
      if (rule.apply(metadata)) { return rule; }
    }
    return null;
  }

  private synchronized EventRule getCached(String hash, SortedMap<String, Object> metadata) {
    EventRule rule = cache.get(hash);
    if (rule == null) {
      rule = findRule(metadata);
      cache.put(hash, rule);
    }
    return rule;
  }

  private String getHash(SortedMap<String, Object> metadata) {
    StringBuffer hash = new StringBuffer();
    boolean first = true;
    for (String keyName : hashKeys) {
      Object key = metadata.get(keyName);
      if (!first) hash.append(HASH_SEPARATOR);
      hash.append(String.valueOf(key));
      first = true;
    }
    return null;
  }

}
