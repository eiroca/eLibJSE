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
package net.eiroca.library.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.Logs;

public class LibRegEx {

  transient private static final Logger logger = Logs.getLogger();
  private static Pattern groupExtractor = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

  public static final int REGEX_TIMELIMIT = 150_000;

  public static String getField(final Matcher m, final String field) {
    String val = null;
    try {
      val = m.group(field);
    }
    catch (IllegalStateException | IllegalArgumentException e) {
    }
    return val != null ? val.trim() : null;
  }

  public static Double getDouble(final Matcher m, final String field) {
    final String val = LibRegEx.getField(m, field);
    return val != null ? Double.valueOf(val) : null;
  }

  public static double getDouble(final Matcher m, final String field, final double def) {
    final String val = LibRegEx.getField(m, field);
    return val != null ? Double.valueOf(val) : def;
  }

  final public static String getGroups(final Matcher matcher, final String[] groupNames, final String sep) {
    // Merge matching group
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (final String group : groupNames) {
      final String newVal = LibRegEx.getGroup(matcher, group);
      if (first) {
        Helper.concatenate(sb, newVal);
        first = false;
      }
      else {
        Helper.concatenate(sb, sep, newVal);
      }
    }
    return sb.toString();
  }

  final public static String getGroup(final Matcher matcher, final String group) {
    String val = null;
    if ((matcher != null) && (LibStr.isNotEmptyOrNull(group))) {
      try {
        int idx = -1;
        final char ch = group.charAt(0);
        if ((ch >= '0') && (ch <= '9')) {
          idx = Helper.getInt(group, -1);
        }
        if ((idx >= 0) && (idx <= matcher.groupCount())) {
          val = matcher.group(idx);
        }
        else {
          val = matcher.group(group);
        }
      }
      catch (final IllegalStateException e) {
        LibRegEx.logger.warn("RegEx error", e);
      }
    }
    return val;
  }

  public static boolean find(final Pattern regEx, String match, final int sizeLimit) {
    boolean result = false;
    final long now = System.nanoTime();
    try {
      if ((sizeLimit > 0) && (sizeLimit < match.length())) {
        match = match.substring(0, sizeLimit);
      }
      result = regEx.matcher(match).find();
    }
    catch (final StackOverflowError err) {
      LibRegEx.logger.warn("Pattern too complex: {}", regEx.pattern());
    }
    final long elapsed = (System.nanoTime() - now);
    if (elapsed >= LibRegEx.REGEX_TIMELIMIT) {
      LibRegEx.logger.info(String.format("find REGEX SLOW: %s", regEx.pattern()));
    }
    return result;
  }

  final public static List<String> getNamedGroup(final String regex) {
    final List<String> namedGroups = new ArrayList<>();
    if (regex != null) {
      final Matcher m = LibRegEx.groupExtractor.matcher(regex);
      while (m.find()) {
        namedGroups.add(m.group(1));
      }
    }
    return namedGroups;
  }

  final public static String expand(final String macro, final Matcher m) {
    if (macro == null) { return null; }
    final StringBuffer sb = new StringBuffer();
    int i = 0;
    final int size = macro.length();
    while (i < (size - 1)) {
      final char ch = macro.charAt(i);
      i++;
      final char nc = macro.charAt(i);
      if (ch == '$') {
        switch (nc) {
          case '$':
            sb.append(ch);
            break;
          case '{':
            String name = null;
            for (int j = i + 1; j < size; j++) {
              if (macro.charAt(j) == '}') {
                name = macro.substring(i + 1, j);
                i = j + 1;
                break;
              }
            }
            if (name != null) {
              final String newVal = LibRegEx.getGroup(m, name);
              if (newVal != null) {
                sb.append(newVal);
              }
            }
            else {
              i = size;
            }
            break;
        }
      }
      else {
        sb.append(ch);
      }
    }
    if (i < size) {
      sb.append(macro.charAt(i));
    }
    return sb.toString();
  }

}
