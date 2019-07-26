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
package net.eiroca.library.rule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.csv.MappingCSVData;
import net.eiroca.library.rule.context.GenericRuleGroup;
import net.eiroca.library.rule.context.LookupRuleGroup;
import net.eiroca.library.rule.context.RegExRuleGroup;
import net.eiroca.library.rule.context.RuleGroup;

public class RuleManager {

  private static final String CSV_ENCODING = "UTF-8";
  private static final char CSV_COMMENT = '#';
  private static final char CSV_SEPARATOR = ';';
  private static final char CSV_QUOTE = '"';

  private static final String SEP = "\t";
  private static final String EOL = "\n";

  public String name;
  Map<String, RuleGroup> registry = new HashMap<>();

  public RuleManager(final String name) {
    super();
    this.name = name;
  }

  public void saveStats(final String path) {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(path));
      for (final RuleGroup context : registry.values()) {
        for (final Rule rule : context.getRules()) {
          bw.write(context.getContextName() + RuleManager.SEP + rule.name + RuleManager.SEP + rule.hits + RuleManager.SEP + rule.count + RuleManager.SEP + rule.elapsed + RuleManager.EOL);
        }
      }
    }
    catch (final IOException e) {
      System.err.println("IOException writing " + path);
    }
    finally {
      Helper.close(bw);
    }
  }

  public void saveMissingKey(final String path) {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(path));
      for (final RuleGroup context : registry.values()) {
        if (context instanceof LookupRuleGroup) {
          final LookupRuleGroup lc = (LookupRuleGroup)context;
          for (final String entry : lc.missingKey) {
            bw.write(context.getContextName() + RuleManager.SEP + entry + RuleManager.EOL);
          }
        }
      }
    }
    catch (final IOException e) {
      System.err.println("IOException writing " + path);
    }
    finally {
      Helper.close(bw);
    }
  }

  public String stringCleanUp(String text, final RegExRuleGroup... replacements) {
    if (LibStr.isEmptyOrNull(text)) { return text; }
    for (final RegExRuleGroup replacement : replacements) {
      if (replacement != null) {
        text = replacement.cleanup(text);
      }
    }
    return text;
  }

  public GenericRuleGroup addGenericRules(final String context, final String path, final int filterCount, final int resultCount) {
    final GenericRuleGroup ruleContext = new GenericRuleGroup(context);
    ruleContext.loadFromCSV(path, RuleManager.CSV_SEPARATOR, RuleManager.CSV_QUOTE, RuleManager.CSV_COMMENT, RuleManager.CSV_ENCODING, filterCount, resultCount);
    registry.put(context, ruleContext);
    return ruleContext;
  }

  public RegExRuleGroup addRegExRules(final String context, final String path) {
    final RegExRuleGroup ruleContext = new RegExRuleGroup(context);
    ruleContext.loadFromCSV(path, RuleManager.CSV_SEPARATOR, RuleManager.CSV_QUOTE, RuleManager.CSV_COMMENT, RuleManager.CSV_ENCODING);
    registry.put(context, ruleContext);
    return ruleContext;
  }

  public LookupRuleGroup addLookupRule(final String context, final String path) {
    final LookupRuleGroup ruleContext = new LookupRuleGroup(context, new MappingCSVData(path, RuleManager.CSV_SEPARATOR, RuleManager.CSV_QUOTE, RuleManager.CSV_COMMENT, RuleManager.CSV_ENCODING));
    registry.put(context, ruleContext);
    return ruleContext;
  }

}
