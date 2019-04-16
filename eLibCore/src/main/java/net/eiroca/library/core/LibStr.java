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
package net.eiroca.library.core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

final public class LibStr {

  private static final String UTF8 = "UTF-8";
  final public static String DEFAULT_ENCODING = System.getProperty("file.encoding", LibStr.UTF8);
  final public static String NL = System.getProperty("line.separator");

  final public static String EMPTY_STRING = "";
  final public static String[] EMPTY_STRINGS = new String[0];

  final public static String upper(final String s) {
    return (s != null) ? s.toUpperCase() : null;
  }

  final public static String lower(final String s) {
    return (s != null) ? s.toLowerCase() : null;
  }

  final public static String limit(final String value, final int limit) {
    final String result;
    if ((value != null) && (limit > 0) && (limit < value.length())) {
      result = value.substring(0, limit);
    }
    else {
      result = value;
    }
    return result;
  }

  final public static boolean isEmptyOrNull(final String str) {
    return ((str == null) || (str.trim().length() == 0));
  }

  final public static boolean isNotEmptyOrNull(final String str) {
    return ((str != null) && (str.trim().length() > 0));
  }

  final public static String concatenateWithSeparator(final String sep, final String nullVal, final Object... objs) {
    final StringBuilder res = new StringBuilder();
    for (int i = 0; i < objs.length; i++) {
      final Object o = objs[i];
      if (i > 0) {
        res.append(sep);
      }
      if (o == null) {
        res.append(nullVal);
      }
      else {
        res.append(String.valueOf(o));
      }
    }
    return res.toString();
  }

  final public static String concatenate(final Object... objs) {
    final StringBuilder res = new StringBuilder();
    for (final Object o : objs) {
      if (o == null) {
        continue;
      }
      res.append(String.valueOf(o));
    }
    return res.toString();
  }

  final public static String toString(final byte[] data) {
    if (Helper.size(data) == 0) {
      return LibStr.EMPTY_STRING;
    }
    else {
      return new String(data);
    }
  }

  final public static String buildString(final byte[] data, final String encoding) {
    String result;
    try {
      result = new String(data, encoding);
    }
    catch (final UnsupportedEncodingException e) {
      result = new String(data);
    }
    return result;
  }

  final public static byte[] convertCharSet(final String s, final String destEncoding) {
    try {
      final Charset charset = Charset.forName(destEncoding);
      final CharsetEncoder encoder = charset.newEncoder();
      final CharBuffer charBuffer = CharBuffer.wrap(s);
      final ByteBuffer bbuf = encoder.encode(charBuffer);
      return bbuf.array();
    }
    catch (final Exception e) {
      return null;
    }
  }

  final public static String getMessage(final byte[] data, final String encoding, final String errString) {
    String body = null;
    try {
      if (data != null) {
        body = new String(data, encoding);
      }
    }
    catch (final UnsupportedEncodingException e) {
      body = String.format(errString, Helper.size(data));
    }
    return body;
  }

  public static boolean is(final String var, final String value) {
    if (LibStr.isEmptyOrNull(var)) { return (value == null ? true : false); }
    return var.equals(value);
  }

  public final static String urlDecode(final String data) {
    if (data == null) { return null; }
    String result;
    try {
      result = URLDecoder.decode(data, LibStr.UTF8);
    }
    catch (final UnsupportedEncodingException e) {
      result = data;
    }
    return result;
  }

  public static String urlEncode(String data) {
    if (data == null) { return null; }
    try {
      data = URLEncoder.encode(data, LibStr.UTF8);
    }
    catch (final UnsupportedEncodingException e) {
      data = "?";
    }
    return data;
  }

  final public static String merge(final List<? extends Object> list, final String sep, final String nullVal) {
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (final Object o : list) {
      if (!first) {
        sb.append(sep);
      }
      sb.append(o != null ? o.toString() : nullVal);
      first = false;
    }
    return sb.toString();
  }

  final public static String merge(final String[] list, final String sep, final String nullVal) {
    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (final String o : list) {
      if (!first) {
        sb.append(sep);
      }
      sb.append(o != null ? o.toString() : nullVal);
      first = false;
    }
    return sb.toString();
  }

  final public static String fixPath(final String path) {
    if (path == null) { return "/"; }
    if (!path.startsWith("/")) { return "/" + path; }
    return path;
  }

  final public static String fixPathWithSlash(final String path) {
    if (path == null) { return "/"; }
    if (!path.endsWith("/")) { return path + "/"; }
    return path;
  }

  final public static void encodeJson(final StringBuilder sb, final String string) {
    if ((string == null) || string.isEmpty()) {
      sb.append("\"\"");
      return;
    }
    char oldCh;
    char ch = 0;
    sb.append('"');
    for (int i = 0; i < string.length(); i++) {
      oldCh = ch;
      ch = string.charAt(i);
      switch (ch) {
        case '\\':
        case '"':
          sb.append('\\');
          sb.append(ch);
          break;
        case '/':
          if (oldCh == '<') {
            sb.append('\\');
          }
          sb.append(ch);
          break;
        case '\b':
        case '\t':
        case '\n':
        case '\f':
        case '\r':
          sb.append(ch);
          break;
        default:
          if ((ch < ' ') || ((ch >= '\u0080') && (ch < '\u00a0')) || ((ch >= '\u2000') && (ch < '\u2100'))) {
            sb.append("\\u");
            final String hhhh = Integer.toHexString(ch);
            for (int l = 0; l < (4 - hhhh.length()); l++) {
              sb.append('0');
            }
            sb.append(hhhh);
          }
          else {
            sb.append(ch);
          }
      }
    }
    sb.append('"');
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

  final public static List<String> splitNL(final String row, final char quote) {
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

  public static String[] toArray(final List<String> row) {
    final String[] res = new String[row.size()];
    for (int i = 0; i < row.size(); i++) {
      res[i] = row.get(i);
    }
    return res;
  }

  public static String getVarName(final String p) {
    final StringBuilder sb = new StringBuilder();
    final int l = p.length();
    if (l > 0) {
      final char c = p.charAt(0);
      if (Character.isJavaIdentifierStart(c)) {
        sb.append(c);
      }
      else {
        sb.append('_');
      }
    }
    for (int i = 1; i < l; i++) {
      final char c = p.charAt(i);
      if (Character.isJavaIdentifierPart(c)) {
        sb.append(c);
      }
      else {
        sb.append('_');
      }
    }
    return sb.toString();
  }

  public static void addAll(final List<String> list, final String[] elems) {
    for (final String s : elems) {
      list.add(s);
    }
  }

  private static boolean isSep(final char ch) {
    return ((ch == ' ') || (ch == '\t') || (ch == '\n') || (ch == '\r'));
  }

  public static List<String> getList(final String valueList, final int num_fields) {
    if ((num_fields == 0) || (valueList == null)) { return null; }
    final List<String> result = new ArrayList<>();
    final int l = valueList.length();
    int s = 0;
    int e = l - 1;
    while ((s < l) && LibStr.isSep(valueList.charAt(s))) {
      s++;
    }
    while ((e > 0) && LibStr.isSep(valueList.charAt(e))) {
      e--;
    }
    if (e > 0) {
      final int max_fields = num_fields > 0 ? num_fields : Integer.MAX_VALUE;
      for (int c = 1; c < max_fields; c++) {
        int cur = s + 1;
        while ((cur <= e) && !LibStr.isSep(valueList.charAt(cur))) {
          cur++;
        }
        result.add(valueList.substring(s, cur));
        s = cur + 1;
        while ((s <= e) && LibStr.isSep(valueList.charAt(s))) {
          s++;
        }
        if (s > e) {
          break;
        }
      }
      if (s <= e) {
        result.add(valueList.substring(s, e + 1));
      }
    }
    else {
      result.add("");
    }
    if ((num_fields > 0) && (result.size() != num_fields)) { return null; }
    return result;
  }

  public static List<String> getList(final String valueList, final char sep, final int num_fields) {
    if ((num_fields == 0) || (valueList == null)) { return null; }
    final List<String> result = new ArrayList<>();
    final int l = valueList.length();
    int s = 0;
    final int e = l - 1;
    if (e > 0) {
      final int max_fields = num_fields > 0 ? num_fields : Integer.MAX_VALUE;
      for (int c = 1; c < max_fields; c++) {
        int cur = s;
        while ((cur <= e) && (sep != valueList.charAt(cur))) {
          cur++;
        }
        result.add(valueList.substring(s, cur));
        s = cur + 1;
        if (s > e) {
          break;
        }
      }
      if (s <= e) {
        result.add(valueList.substring(s, e + 1));
      }
    }
    else {
      result.add("");
    }
    if ((num_fields > 0) && (result.size() != num_fields)) { return null; }
    return result;
  }

}
