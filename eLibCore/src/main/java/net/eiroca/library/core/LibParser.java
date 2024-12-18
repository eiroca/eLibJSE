/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
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
import net.eiroca.library.data.Tags;

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
      if (s <= l) {
        result.add(row.substring(s));
      }
    }
    else {
      result.add(LibParser.EMPTY);
    }
    if ((num_fields > 0) && (result.size() != num_fields)) { return null; }
    return result;
  }

  private enum WebLogStates {
    START, IN_EMPTYFIELD, IN_FIELD, IN_BRACKET, IN_QUOTE, QUOTEESCAPE
  }

  public static List<String> splitWebLog(final String row) {
    if (row == null) { return null; }
    final List<String> result = new ArrayList<>();
    final int len = row.length();
    int pos = 0;
    int start = 0;
    WebLogStates state = WebLogStates.START;
    StringBuilder sb = null;
    while (pos < len) {
      final char ch = row.charAt(pos);
      switch (state) {
        case START:
          // space
          if ((ch != ' ') && (ch != '\t')) {
            if (ch == '[') {
              state = WebLogStates.IN_BRACKET;
              start = pos + 1;
            }
            else if (ch == '\"') {
              state = WebLogStates.IN_QUOTE;
              start = pos + 1;
            }
            else if (ch == '-') {
              state = WebLogStates.IN_EMPTYFIELD;
              start = pos;
            }
            else {
              state = WebLogStates.IN_FIELD;
              start = pos;
            }
          }
          break;
        case IN_EMPTYFIELD:
          if ((ch == ' ') || (ch == '\t')) {
            result.add(LibParser.EMPTY);
            state = WebLogStates.START;
          }
          else {
            state = WebLogStates.IN_FIELD;
          }
          break;
        case IN_FIELD:
          // normal field
          if ((ch == ' ') || (ch == '\t')) {
            state = WebLogStates.START;
            result.add(row.substring(start, pos));
          }
          break;
        case IN_BRACKET:
          // [] field
          if ((ch == ']')) {
            state = WebLogStates.START;
            result.add(row.substring(start, pos));
          }
          break;
        case IN_QUOTE:
          // "" field
          if ((ch == '\"')) {
            if ((pos < (len - 1)) && (row.charAt(pos + 1) == '\"')) {
              // escaped quote with ""
              state = WebLogStates.QUOTEESCAPE;
              if (sb == null) {
                sb = new StringBuilder(len);
              }
              else {
                sb.setLength(0);
              }
              pos++;
              sb.append(row.substring(start, pos));
            }
            else {
              state = WebLogStates.START;
              result.add(row.substring(start, pos));
            }
          }
          if ((ch == '\\')) {
            if ((pos < (len - 1)) && (row.charAt(pos + 1) == '\"')) {
              // escaped quote with \"
              state = WebLogStates.QUOTEESCAPE;
              if (sb == null) {
                sb = new StringBuilder(len);
              }
              else {
                sb.setLength(0);
              }
              sb.append(row.substring(start, pos));
              sb.append('\"');
              pos++;
            }
            else {
              state = WebLogStates.START;
              result.add(row.substring(start, pos));
            }
          }
          break;
        case QUOTEESCAPE:
          if ((ch == '\"')) {
            if (pos < (len - 1)) {
              if (row.charAt(pos + 1) == '\"') {
                sb.append(ch);
                pos++;
                break;
              }
            }
            state = WebLogStates.START;
            result.add(sb.toString());
          }
          else if ((ch == '\\')) {
            if (pos < (len - 1)) {
              if (row.charAt(pos + 1) == '\"') {
                sb.append('\"');
                pos++;
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
      pos++;
    }
    switch (state) {
      case START:
        break;
      case IN_EMPTYFIELD:
        result.add(LibParser.EMPTY);
        break;
      case IN_FIELD:
      case IN_BRACKET:
        result.add(row.substring(start));
        break;
      case IN_QUOTE:
      case QUOTEESCAPE:
        if (sb != null) {
          result.add(sb.toString());
        }
        else {
          result.add(LibParser.EMPTY);
        }
        break;
    }
    return result;
  }

  // extract max + 1 field separated by space and unlimited tags surrounded by [] 
  // nested [] are supported [a[]] -> tag = a[]
  // with max=3
  // field1 field2 [tag1] [tag[2]] [tag3] field3 last field
  // output list is: "filed1" "field2" "tag1" "tag[2]" "tag3" "field3" "last field"
  // null in -> null out
  private enum AltLogStates {
    INITAIL, IN_FIELD, IN_TAG, LAST
  }

  public static List<String> splitAltLog(final String row, final int max, final char sep, final char bracketOpen, final char bracketClose) {
    if (row == null) { return null; }
    final List<String> result = new ArrayList<>();
    final int len = row.length();
    int level = 0;
    int pos = 0;
    int start = 0;
    int cnt = 0;
    AltLogStates state = AltLogStates.INITAIL;
    while (pos < len) {
      final char ch = row.charAt(pos);
      switch (state) {
        case INITAIL:
          // space
          if (ch != sep) {
            if (ch == bracketOpen) {
              state = AltLogStates.IN_TAG;
              start = pos + 1;
            }
            else {
              cnt++;
              start = pos;
              if (cnt > max) {
                state = AltLogStates.LAST;
              }
              else {
                state = AltLogStates.IN_FIELD;
              }
            }
          }
          break;
        case IN_FIELD:
          // normal field
          if (ch == sep) {
            state = AltLogStates.INITAIL;
            result.add(row.substring(start, pos));
          }
          break;
        case IN_TAG:
          // [] field
          if ((ch == bracketClose)) {
            if (level == 0) {
              state = AltLogStates.INITAIL;
              result.add(row.substring(start, pos));
            }
            else {
              level--;
            }
          }
          else if (ch == bracketOpen) {
            level++;
          }
          break;
        case LAST:
          pos = len - 1;
          break;
      }
      pos++;
    }
    if (state != AltLogStates.INITAIL) {
      result.add(row.substring(start, pos));
    }
    return result;
  }

  private enum OptMsgStates {
    START, OPT, TRANSITION, SKIP, MESSAGE
  }

  // Split:
  // "opt : message" -> "opt", "message"
  // "opt - message" -> "opt", "message"
  // "opt: message"  -> "opt", "message"
  // "opt- message"  -> "opt", "message"
  // " opt: message" -> "opt", "message"
  // ": message"     -> "", "message"
  // " - message"    -> "", "message"
  // "mess:a: g e"   -> "mess:a: g e"
  // "  message"     -> "message"
  // null in -> null out
  public static List<String> splitOptAndMessage(final String row, final char sep, char sp1, char sp2) {
    if (row == null) { return null; }
    final List<String> result = new ArrayList<>();
    final int len = row.length();
    int pos = 0;
    int start = 0;
    int last = 0;
    OptMsgStates state = OptMsgStates.START;
    while (pos < len) {
      final char ch = row.charAt(pos);
      switch (state) {
        case START:
          if (ch == sep) {
            start++;
          }
          else if ((ch == sp1) || (ch == sp2)) {
            state = OptMsgStates.TRANSITION;
          }
          else {
            state = OptMsgStates.OPT;
            last++;
          }
          break;
        case OPT:
          if (ch == sep) {
          }
          else if ((ch == sp1) || (ch == sp2)) {
            state = OptMsgStates.TRANSITION;
          }
          else {
            last++;
          }
          break;
        case TRANSITION:
          if (ch == sep) {
            // valid opt
            result.add(row.substring(start, last));
            state = OptMsgStates.SKIP;
          }
          else {
            state = OptMsgStates.MESSAGE;
          }
          break;
        case SKIP:
          if (ch != sep) {
            start = pos;
            state = OptMsgStates.MESSAGE;
          }
          break;
        case MESSAGE:
          pos = len - 1;
          break;
      }
      pos++;
    }
    if (state != OptMsgStates.START) {
      result.add(row.substring(start, pos));
    }
    return result;
  }

  private enum TagAndMessageStates {
    START, IN_VALUE, IN_NAMEVALUE, MESSAGE
  }

  //[tagValue1] [tagValue2] [tagA: tagValue[]] [tagB: tagValue] messag[e]
  //Names: "F1", "F2"
  //output: "F1:tagValue1", "F2:tagValue2", "tagA:tagValue[]", "tagB:tagValue", "5:messag[e]"
  //[ ] [ a : x ]
  //output: "F1:", "a:x"
  // null in -> null out
  public static Tags splitTagAndMessage(final String row, char sep, final char bracketOpen, final char bracketClose, final char bracketSep, String[] names) {
    if (row == null) { return null; }
    final Tags result = new Tags();
    final int len = row.length();
    int level = 0;
    int pos = 0;
    int start = 0;
    int startBraket = 0;
    int last = 0;
    String name = null;
    String val = null;
    int count = 0;
    boolean empty = true;
    TagAndMessageStates state = TagAndMessageStates.START;
    while (pos < len) {
      final char ch = row.charAt(pos);
      switch (state) {
        case START:
          if (ch == sep) {
            start++;
          }
          else if (ch == bracketOpen) {
            startBraket = pos;
            start = pos + 1;
            state = TagAndMessageStates.IN_VALUE;
            empty = true;
          }
          else {
            state = TagAndMessageStates.MESSAGE;
            last++;
          }
          break;
        case IN_VALUE:
          if (ch == sep) {
            if (empty) start++;
          }
          else if (ch == bracketClose) {
            if (level == 0) {
              name = getName(names, count);
              count++;
              val = row.substring(start, pos);
              result.add(name, val);
              start = pos + 1;
            }
            else {
              level--;
            }
            last = pos + 1;
            state = TagAndMessageStates.START;
          }
          else {
            if (ch == bracketOpen) {
              last = pos + 1;
              level++;
            }
            else if ((ch == bracketSep) && (level == 0)) {
              state = TagAndMessageStates.IN_NAMEVALUE;
              name = row.substring(start, last);
              start = pos + 1;
              empty = true;
            }
            else {
              empty = false;
              last = pos + 1;
            }
          }
          break;
        case IN_NAMEVALUE:
          if (ch == sep) {
            if (empty) start++;
          }
          else if (ch == bracketClose) {
            if (level == 0) {
              count++;
              val = row.substring(start, last);
              result.add(name, val);
              state = TagAndMessageStates.START;
              start = pos + 1;
            }
            else {
              level--;
            }
            last = pos + 1;
          }
          else {
            last = pos + 1;
            if (ch == bracketOpen) {
              level++;
            }
            empty = false;
          }
          break;
        case MESSAGE:
          pos = len - 1;
          break;
      }
      pos++;
    }
    switch (state) {
      case MESSAGE:
        if (start < len) {
          name = getName(names, count);
          val = row.substring(start, len);
          result.add(name, val);
        }
        break;
      case IN_VALUE:
      case IN_NAMEVALUE:
        name = getName(names, count);
        val = row.substring(startBraket, len);
        result.add(name, val);
        break;
      default:
        break;

    }
    return result;
  }

  private static String getName(String[] names, int count) {
    String result;
    if ((names != null) && (names.length > count)) {
      result = names[count];
    }
    else {
      result = String.valueOf(count + 1);
    }
    return result;
  }

}
