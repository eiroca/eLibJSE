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
package net.eiroca.library.rule.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.csv.CSVData;
import net.eiroca.library.rule.GenericRule;
import net.eiroca.library.rule.Rule;

public class GenericRuleGroup extends RuleGroup {

  public GenericRuleGroup(final String name) {
    super(name);
  }

  private final List<GenericRule> rules = new ArrayList<>();

  @Override
  public Collection<? extends Rule> getRules() {
    return rules;
  }

  public void loadFromCSV(final String path, final char sep, final char quote, final char com, final String enc, final int filterCount, final int resultCount) {
    final CSVData data = new CSVData(path, sep, quote, com, enc);
    final int totCount = filterCount + resultCount;
    for (final String[] fixDef : data.getData()) {
      if (fixDef.length != totCount) {
        System.err.println("DEFINIZIONE ERRATA " + getContextName() + ": " + LibStr.merge(fixDef, ",", ""));
        continue;
      }
      final String[] filter = new String[filterCount];
      final String[] result = new String[resultCount];
      for (int i = 0; i < filterCount; i++) {
        filter[i] = LibStr.isNotEmptyOrNull(fixDef[i]) ? fixDef[i] : null;
      }

      for (int i = 0, j = filterCount; i < resultCount; i++, j++) {
        result[i] = LibStr.isNotEmptyOrNull(fixDef[j]) ? fixDef[j] : null;
      }
      final GenericRule rule = new GenericRule(LibStr.merge(filter, ",", ""), filter, result);
      rules.add(rule);
    }
  }

}
