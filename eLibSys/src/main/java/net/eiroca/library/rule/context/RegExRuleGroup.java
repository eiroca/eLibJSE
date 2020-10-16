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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.csv.CSVData;
import net.eiroca.library.data.MultipleWords;
import net.eiroca.library.rule.RegExRule;
import net.eiroca.library.rule.Rule;

public class RegExRuleGroup extends RuleGroup {

  private CSVData definitions;

  private final HashMap<String, RegExRule> regexes = new HashMap<>();

  public RegExRuleGroup(final String name) {
    super(name);
  }

  public synchronized RegExRule getPattern(final String regex) {
    RegExRule r = regexes.get(regex);
    if (r == null) {
      final Pattern p = Pattern.compile(regex);
      r = new RegExRule(p);
      regexes.put(regex, r);
    }
    return r;
  }

  public String replaceAll(final String text, final String oldStr, final String newStr) {
    final RegExRule r = getPattern(oldStr);
    return r.replaceAll(text, newStr);
  }

  public List<String> getGroup(final String text, final String pattern, final int group) {
    final RegExRule r = getPattern(pattern);
    return r.getAllGroup(text, group);
  }

  public boolean find(final String text, final String pattern) {
    final RegExRule r = getPattern(pattern);
    return r.find(text);
  }

  public void searchTags(final String text, final MultipleWords tags, final boolean addUnknown) {
    if (LibStr.isEmptyOrNull(text)) { return; }
    boolean found = false;
    final CSVData tagPatterns = definitions;
    if (tagPatterns.getFieldNames().size() != 3) {
      System.err.println("DEFINIZIONE ERRATA: " + LibStr.merge(tagPatterns.getFieldNames(), ",", ""));
      return;
    }
    for (final String[] curDef : tagPatterns.getData()) {
      String textStr = text;
      final List<String> replacements = getGroup(text, curDef[0], 1);
      for (final String replacement : replacements) {
        if (LibStr.isNotEmptyOrNull(replacement)) {
          textStr = MessageFormat.format(curDef[1], replacement);
          if (LibStr.isNotEmptyOrNull(textStr)) {
            final double w = Helper.getDouble(curDef[2], 1);
            tags.add(textStr, w);
          }
          found = true;
        }
      }
    }
    if (!found && addUnknown) {
      tags.add(text, 0.1);
    }
  }

  public String cleanup(String text) {
    for (final String[] sost : definitions.getData()) {
      final String oldStr = sost[0];
      final String newStr = sost[1];
      try {
        text = replaceAll(text, oldStr, newStr);
      }
      catch (IndexOutOfBoundsException | IllegalArgumentException e) {
        System.err.println(oldStr + " -> " + newStr);
        System.err.println("Invalid rule: " + e.getMessage());
      }
    }
    return text;
  }

  @Override
  public Collection<? extends Rule> getRules() {
    return regexes.values();
  }

  public void loadFromCSV(final String path, final char sep, final char quote, final char com, final String enc) {
    setDefinitions(new CSVData(path, sep, quote, com, enc));
  }

  public CSVData getDefinitions() {
    return definitions;
  }

  public void setDefinitions(final CSVData newData) {
    if ((newData.getFieldNames().size() < 2)) {
      System.err.println("INVALID REPLACE DEFINITION (at least 2 colomns)");
      return;
    }
    definitions = newData;
  }

}
