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
package net.eiroca.library.core;

import java.util.ArrayList;
import java.util.List;

public class LibParser {

  private static final String EMPTY = "";

  private static boolean isSep(final char ch) {
    return ((ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r'));
  }

  final public static List<String> split(final String row, final char separator, final char quote) {
    final List<String> result = new ArrayList<>();
    final StringBuffer sb = new StringBuffer();
    boolean inQuote = false;
    for (int i = 0; i < row.length(); i++) {
      final char ch = row.charAt(i);
      if (inQuote) {
        if (ch == quote) {
          char nextCh = 0;
          if (i < (row.length() - 1)) {
            nextCh = row.charAt(i + 1);
          }
          if (nextCh == quote) {
            sb.append(ch);
            i++;
          }
          else {
            inQuote = false;
          }
        }
        else {
          sb.append(ch);
        }
      }
      else {
        if (ch == separator) {
          result.add(sb.toString());
          sb.setLength(0);
        }
        else if (ch == quote) {
          inQuote = true;
        }
        else {
          sb.append(ch);
        }
      }
    }
    result.add(sb.toString());
    return result;
  }

  final public static List<String> splitWithNL(final String row, final char quote) {
    final List<String> result = new ArrayList<>();
    final StringBuffer sb = new StringBuffer();
    boolean inQuote = false;
    char lc = 0;
    for (int i = 0; i < row.length(); i++) {
      final char ch = row.charAt(i);
      if (inQuote) {
        if (ch == quote) {
          char nextCh = 0;
          if (i < (row.length() - 1)) {
            nextCh = row.charAt(i + 1);
          }
          if (nextCh == quote) {
            sb.append(ch);
            i++;
          }
          else {
            inQuote = false;
          }
        }
        else {
          sb.append(ch);
        }
      }
      else {
        if ((ch == '\n') || (ch == '\r')) {
          if ((ch == '\n') && (lc == '\r')) {
          }
          else if ((ch == '\r') && (lc == '\n')) {
          }
          else {
            result.add(sb.toString());
            sb.setLength(0);
            lc = ch;
          }
        }
        else if (ch == quote) {
          lc = 0;
          inQuote = true;
        }
        else {
          lc = 0;
          sb.append(ch);
        }
      }
    }
    result.add(sb.toString());
    return result;
  }

  public static List<String> splitWithSpaces(final String row, final int num_fields) {
    if ((num_fields == 0) || (row == null)) { return null; }
    final List<String> result = new ArrayList<>();
    final int l = row.length();
    int s = 0;
    int e = l - 1;
    while ((s < l) && LibParser.isSep(row.charAt(s))) {
      s++;
    }
    while ((e > 0) && LibParser.isSep(row.charAt(e))) {
      e--;
    }
    if (e > 0) {
      final int max_fields = num_fields > 0 ? num_fields : Integer.MAX_VALUE;
      for (int c = 1; c < max_fields; c++) {
        int cur = s + 1;
        while ((cur <= e) && !LibParser.isSep(row.charAt(cur))) {
          cur++;
        }
        result.add(row.substring(s, cur));
        s = cur + 1;
        while ((s <= e) && LibParser.isSep(row.charAt(s))) {
          s++;
        }
        if (s > e) {
          break;
        }
      }
      if (s <= e) {
        result.add(row.substring(s, e + 1));
      }
    }
    else {
      result.add(LibParser.EMPTY);
    }
    if ((num_fields > 0) && (result.size() != num_fields)) { return null; }
    return result;
  }

  public static List<String> splitWithSep(final String row, final char sep, final int num_fields) {
    if ((num_fields == 0) || (row == null)) { return null; }
    final List<String> result = new ArrayList<>();
    final int l = row.length();
    int s = 0;
    final int e = l - 1;
    if (e > 0) {
      final int max_fields = num_fields > 0 ? num_fields : Integer.MAX_VALUE;
      for (int c = 1; c < max_fields; c++) {
        int cur = s;
        while ((cur <= e) && (sep != row.charAt(cur))) {
          cur++;
        }
        result.add(row.substring(s, cur));
        s = cur + 1;
        if (s > e) {
          break;
        }
      }
      if (s <= e) {
        result.add(row.substring(s, e + 1));
      }
    }
    else {
      result.add(LibParser.EMPTY);
    }
    if ((num_fields > 0) && (result.size() != num_fields)) { return null; }
    return result;
  }

  public static List<String> splitWebLog(final String row) {
    if (row == null) { return null; }
    final List<String> result = new ArrayList<>();
    final int l = row.length();
    int i = 0;
    int s = 0;
    int state = 0;
    StringBuilder sb = null;
    while (i < l) {
      final char ch = row.charAt(i);
      switch (state) {
        case 0:
          // space
          if ((ch != ' ') && (ch != '\t')) {
            if (ch == '-') {
              result.add(LibParser.EMPTY);
            }
            else if (ch == '[') {
              state = 2;
              s = i + 1;
            }
            else if (ch == '\"') {
              state = 3;
              s = i + 1;
            }
            else {
              state = 1;
              s = i;
            }
          }
          break;
        case 1:
          // normal field
          if ((ch == ' ') || (ch == '\t')) {
            state = 0;
            result.add(row.substring(s, i));
          }
          break;
        case 2:
          // [] field
          if ((ch == ']')) {
            state = 0;
            result.add(row.substring(s, i));
          }
          break;
        case 3:
          // "" field
          if ((ch == '\"')) {
            if ((i < (l - 1)) && (row.charAt(i + 1) == '\"')) {
              // escaped quote with ""
              state = 4;
              if (sb == null) {
                sb = new StringBuilder(l);
              }
              else {
                sb.setLength(0);
              }
              i++;
              sb.append(row.substring(s, i));
            }
            else {
              state = 0;
              result.add(row.substring(s, i));
            }
          }
          if ((ch == '\\')) {
            if ((i < (l - 1)) && (row.charAt(i + 1) == '\"')) {
              // escaped quote with \"
              state = 4;
              if (sb == null) {
                sb = new StringBuilder(l);
              }
              else {
                sb.setLength(0);
              }
              sb.append(row.substring(s, i));
              sb.append('\"');
              i++;
            }
            else {
              state = 0;
              result.add(row.substring(s, i));
            }
          }
          break;
        case 4:
          if ((ch == '\"')) {
            if (i < (l - 1)) {
              if (row.charAt(i + 1) == '\"') {
                sb.append(ch);
                i++;
                break;
              }
            }
            state = 0;
            result.add(sb.toString());
          }
          else if ((ch == '\\')) {
            if (i < (l - 1)) {
              if (row.charAt(i + 1) == '\"') {
                sb.append('\"');
                i++;
                break;
              }
            }
            sb.append(ch);
          }
          else {
            sb.append(ch);
          }
          break;
      }
      i++;
    }
    if (state > 0) {
      if (state == 4) {
        if (sb != null) {
          result.add(sb.toString());
        }
        else {
          result.add(LibParser.EMPTY);
        }
      }
      else {
        result.add(row.substring(s, i));
      }
    }
    return result;
  }

}
