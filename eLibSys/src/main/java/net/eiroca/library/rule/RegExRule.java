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
package net.eiroca.library.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExRule extends Rule {

  transient Pattern pattern;

  public RegExRule(final Pattern pattern) {
    super();
    this.pattern = pattern;
    name = pattern.pattern();
  }

  public String replaceAll(String text, final String newStr) {
    final long startTime = System.currentTimeMillis();
    final Matcher m = pattern.matcher(text);
    count++;
    if (m.find()) {
      hits++;
      text = m.replaceAll(newStr);
    }
    elapsed += System.currentTimeMillis() - startTime;
    return text;
  }

  public String getGroup(final String text, final int group) {
    final long startTime = System.currentTimeMillis();
    final Matcher m = pattern.matcher(text);
    String result = null;
    count++;
    if (m.find()) {
      hits++;
      result = m.group(group);
    }
    elapsed += System.currentTimeMillis() - startTime;
    return result;
  }

  public List<String> getAllGroup(final String text, final int group) {
    final long startTime = System.currentTimeMillis();
    final List<String> res = new ArrayList<>();
    final Matcher m = pattern.matcher(text);
    count++;
    while (m.find()) {
      res.add(m.group(group));
      hits++;
    }
    elapsed += System.currentTimeMillis() - startTime;
    return res;
  }

  public boolean find(final String text) {
    final long startTime = System.currentTimeMillis();
    final Matcher m = pattern.matcher(text);
    final boolean result = m.find();
    count++;
    hits += result ? 1 : 0;
    elapsed += System.currentTimeMillis() - startTime;
    return result;
  }

  public Matcher getMatcher(final String text) {
    final long startTime = System.currentTimeMillis();
    final Matcher m = pattern.matcher(text);
    count++;
    elapsed += System.currentTimeMillis() - startTime;
    return m;
  }

}
