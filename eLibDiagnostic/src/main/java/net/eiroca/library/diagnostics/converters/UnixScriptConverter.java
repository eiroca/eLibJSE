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
package net.eiroca.library.diagnostics.converters;

import java.util.regex.Pattern;

public class UnixScriptConverter extends BaseConverter {

  private static final String SPECIAL_CHARS_UNIX = "!@$^&*~?.|/[]<> `\";#()\\";
  private static final String REGEX_UNIX = "['!@$^&*~?.|/\\[\\]<> `\";#()\\\\]";
  private static final Pattern pUnix = Pattern.compile(UnixScriptConverter.REGEX_UNIX);
  private static final String ESCAPE_CHAR_Q_STRING = "\'";
  private static final char ESCAPE_CHAR_Q = '\'';
  private static final char ESCAPE_CHAR_DQ = '"';

  @Override
  public String convert(final String source) {
    if (source == null) { return BaseConverter.NULL; }
    if (UnixScriptConverter.pUnix.matcher(source).find()) {
      return UnixScriptConverter.escapeStringUnix(source);
    }
    else {
      return source;
    }
  }

  public static String escapeStringUnix(final String ins) {
    final StringBuilder sb = new StringBuilder();
    final String[] as = ins.split(UnixScriptConverter.ESCAPE_CHAR_Q_STRING, -1);
    for (int i = 0; i < as.length; i++) {
      final String s = as[i];
      if (i > 0) {
        sb.append(UnixScriptConverter.ESCAPE_CHAR_DQ).append(UnixScriptConverter.ESCAPE_CHAR_Q).append(UnixScriptConverter.ESCAPE_CHAR_DQ);
      }
      if (!s.isEmpty()) {
        sb.append(UnixScriptConverter.escapeSubstringUnix(s, UnixScriptConverter.SPECIAL_CHARS_UNIX, UnixScriptConverter.ESCAPE_CHAR_Q));
      }
    }
    return sb.toString();
  }

  public static String escapeSubstringUnix(final String ins, final String specialChars, final char quote) {
    int start = 0;
    String s = ins;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < specialChars.length(); i++) {
      final char ch = specialChars.charAt(i);
      while (start < s.length()) {
        int found = s.indexOf(ch, start);
        if (found > -1) {
          sb.append(s.substring(start, found)).append(quote).append(ch).append(quote);
          start = ++found;
        }
        else {
          if (start == 0) {
            break;
          }
          // add the rest of the s string
          sb.append(s.substring(start));
          start = s.length();
        }
      }
      // reset vars for the next special char
      if (start != 0) {
        s = sb.toString();
        sb = new StringBuilder();
      }
      start = 0;
    }
    return s;
  }

}
