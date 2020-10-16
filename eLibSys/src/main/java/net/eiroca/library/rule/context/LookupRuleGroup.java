/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.rule.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.eiroca.library.csv.MappingCSVData;
import net.eiroca.library.rule.LookupRule;
import net.eiroca.library.rule.Rule;

public class LookupRuleGroup extends RuleGroup {

  public LookupRule lookupTable = new LookupRule();

  public LookupRuleGroup(final String name, final MappingCSVData data) {
    super(name);
    lookupTable.name = name;
    lookupTable.table = data.getValuesMap();
  }

  @Override
  public Collection<? extends Rule> getRules() {
    final List<Rule> rules = new ArrayList<>();
    rules.add(lookupTable);
    return rules;
  }

  public Map<String, String> lookup(String key) {
    if (key == null) { return null; }
    key = key.toLowerCase();
    final Map<String, String> result = lookupTable.lookup(key);
    if (result == null) {
      missingKey.add(key);
    }
    return result;
  }

}
