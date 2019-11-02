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
package net.eiroca.library.sysadm.monitoring.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import net.eiroca.library.core.LibFormat;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.sysadm.monitoring.api.DatumCheck;
import net.eiroca.library.sysadm.monitoring.api.EventFilter;
import net.eiroca.library.sysadm.monitoring.api.EventRule;
import net.eiroca.library.system.Logs;

public class RuleEngine {

  private static final Logger logger = Logs.getLogger();

  private static final char HASH_SEPARATOR = '\t';
  private static final double ZERO = 0.000001;
  private final Set<String> hashKeys = new HashSet<>();
  private final List<EventRule> ruleSet = new ArrayList<>();
  private final Map<String, EventRule> cache = new HashMap<>();

  public void loadRules(final Properties config) {
    final Map<String, EventRule> alias = new TreeMap<>();
    for (final String name : config.stringPropertyNames()) {
      String val = config.getProperty(name);
      final String[] command = name.split("\\.", -1);
      String param = null;
      if (command.length < 2) {
        continue;
      }
      if (command.length == 3) {
        param = command[2];
      }
      final String ruleName = command[0];
      EventRule rule = alias.get(ruleName);
      if (rule == null) {
        rule = new EventRule();
        alias.put(ruleName, rule);
      }
      final String ruleType = command[1];
      boolean processed = true;
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
            if (val.startsWith("f")) {
              enable = false;
            }
            else if (val.equals("off")) {
              enable = false;
            }
            else if (val.equals("disabled")) {
              enable = false;
            }
            else if (val.equals("0")) {
              enable = false;
            }
          }
          if ((param == null) || ("*".equals(param)) || ("all".equals(param.toLowerCase()))) {
            rule.connectors(enable);
          }
          else {
            rule.connector(param, enable);
          }
          break;
        default:
          processed = false;
          break;
      }
      if (!processed) {
        if (LibFormat.STRVALUE.containsKey(ruleType)) {
          final double w = LibFormat.STRVALUE.get(ruleType);
          if (param != null) {
            final Double vl = LibFormat.getValue(val);
            if (vl != null) {
              DatumCheck chk = null;
              switch (param.toLowerCase()) {
                case "min":
                case "minimum":
                case "minvalue":
                case ">":
                case ">=":
                  chk = new DatumCheck(ruleType, w, vl, null);
                  break;
                case "max":
                case "maximum":
                case "maxvalue":
                case "<":
                case "<=":
                  chk = new DatumCheck(ruleType, w, null, vl);
                  break;
                case "equal":
                case "=":
                case "==":
                  chk = new DatumCheck(ruleType, w, vl - RuleEngine.ZERO, vl + RuleEngine.ZERO);
                  break;
              }
              if (chk != null) {
                rule.addCheck(chk);
              }
            }
          }
        }
      }
    }
    for (final EventRule rule : alias.values()) {
      addRule(rule);
    }
  }

  public void addRule(final EventRule rule) {
    ruleSet.add(rule);
    final List<EventFilter> filters = rule.getFilters();
    if (filters != null) {
      for (final EventFilter filter : filters) {
        hashKeys.add(filter.keyName);
      }
    }
  }

  public EventRule ruleFor(final SortedMap<String, Object> metadata) {
    final String hash = getHash(metadata);
    final EventRule result = getCached(hash, metadata);
    return result;

  }

  private EventRule findRule(final SortedMap<String, Object> metadata) {
    EventRule result = null;
    for (final EventRule rule : ruleSet) {
      if (rule.apply(metadata)) {
        result = rule;
        break;
      }
    }
    RuleEngine.logger.debug("findRule: " + metadata + " -> " + result);
    return result;
  }

  private synchronized EventRule getCached(final String hash, final SortedMap<String, Object> metadata) {
    EventRule rule = cache.get(hash);
    if (rule == null) {
      rule = findRule(metadata);
      cache.put(hash, rule);
    }
    return rule;
  }

  private String getHash(final SortedMap<String, Object> metadata) {
    final StringBuffer hash = new StringBuffer();
    boolean first = true;
    for (final String keyName : hashKeys) {
      final Object key = metadata.get(keyName);
      if (!first) {
        hash.append(RuleEngine.HASH_SEPARATOR);
      }
      hash.append(String.valueOf(key));
      first = true;
    }
    return hash.toString();
  }

}
