/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0;
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
package net.eiroca.library.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.eiroca.library.core.LibStr;

public class RegExJava extends ARegEx {

  public Pattern regEx;

  public RegExJava(final String name, final String pattern) throws Exception {
    super(name, pattern);
    regEx = Pattern.compile(pattern);
  }

  private Matcher getMatcher(final String text) {
    final String match = LibStr.limit(text, sizeLimit);
    boolean success = false;
    Matcher matcher = null;
    tic();
    try {
      matcher = regEx.matcher(match);
      success = matcher.find();
    }
    catch (final StackOverflowError err) {
      ruleFail();
    }
    toc(success);
    return success ? matcher : null;
  }

  @Override
  public boolean find(final String text) {
    final Matcher matcher = getMatcher(text);
    return (matcher != null);
  }

  @Override
  public String findFirst(final String text) {
    final Matcher matcher = getMatcher(text);
    final String value = (matcher != null) ? matcher.group(1) : null;
    return value;
  }

  @Override
  public List<String> extract(final List<String> namedFields, final String text) {
    List<String> result = null;
    final Matcher matcher = getMatcher(text);
    if (matcher != null) {
      final int size = namedFields.size();
      result = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        result.add(matcher.group(namedFields.get(i)));
      }
    }
    return result;
  }

  @Override
  public List<String> extract(final String text) {
    List<String> result = null;
    final Matcher matcher = getMatcher(text);
    if (matcher != null) {
      final int size = matcher.groupCount() + 1;
      result = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        result.add(matcher.group(i));
      }
    }
    return result;
  }

}
