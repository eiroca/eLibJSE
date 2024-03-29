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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.List;

final public class LibStr {

  public static final String UTF8 = "UTF-8";

  public static final String DEFAULT_ENCODING = System.getProperty("file.encoding", LibStr.UTF8);
  public static final String NL = System.getProperty("line.separator");

  public static final String VALUE_SEP = "=";
  public static final String LIST_SEP = ",";

  public static final String EMPTY_STRING = "";
  public static final String[] EMPTY_STRINGS = new String[0];

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

  final public static void encodeJson(final StringBuilder sb, final String value) {
    if ((value == null) || value.isEmpty()) {
      sb.append("\"\"");
      return;
    }
    char oldCh;
    char ch = 0;
    sb.append('"');
    for (int i = 0; i < value.length(); i++) {
      oldCh = ch;
      ch = value.charAt(i);
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

  public static String[] toArray(final List<String> row) {
    final String[] res = new String[row.size()];
    for (int i = 0; i < row.size(); i++) {
      res[i] = row.get(i);
    }
    return res;
  }

  public static String toString(final Iterable<?> objects, final String sep) {
    final StringBuilder buf = new StringBuilder();
    if (objects != null) {
      for (final Object o : objects) {
        if (buf.length() > 0) {
          buf.append(sep);
        }
        buf.append(String.valueOf(o));
      }
    }
    return buf.toString();
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

  public static void encodeJava(final StringBuilder sb, final String string) {
    if ((string == null) || string.isEmpty()) { return; }
    char ch = 0;
    for (int i = 0; i < string.length(); i++) {
      ch = string.charAt(i);
      switch (ch) {
        case '\\':
        case '"':
          sb.append('\\');
          sb.append(ch);
          break;
        case '\b':
        case '\t':
        case '\n':
        case '\f':
        case '\r':
          sb.append('\\');
          sb.append((char)(ch + 64));
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
  }

  public static HashMap<String, Integer> parseMapping(final String mapping) throws Exception {
    return LibStr.parseMapping(mapping, LibStr.LIST_SEP, LibStr.VALUE_SEP);
  }

  public static HashMap<String, Integer> parseMapping(final String mapping, final String listSep, final String valueSep) throws Exception {
    final HashMap<String, Integer> map = new HashMap<>();
    for (final String mapEntry : mapping.split(listSep)) {
      final String[] valPair = mapEntry.split(valueSep);
      if (valPair.length == 2) {
        final String name = valPair[0].toLowerCase();
        final int val = Integer.parseInt(valPair[1]);
        map.put(name, val);
      }
    }
    return map;
  }

}
