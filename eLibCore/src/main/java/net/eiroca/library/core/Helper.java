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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import net.eiroca.library.data.SortedProperties;

final public class Helper {

  public static final String DEFAULT_ENCODING = System.getProperty("file.encoding", "UTF-8");
  public static final String NL = System.getProperty("line.separator", "\n");
  public static final String OS_NAME = System.getProperty("os.name", "Unknown");
  public static final String LISTSEPERATOR = System.getProperty("list.separator", ";");

  public static final String EMPTY_STRING = "";
  public static final String DOUBLE_QUOTE = "\"";
  public static final String NO_DATA = "-";
  public final static String MINUS = "-";

  private static final String SPLIT_SEPARATOR = "(\n|\r)+";

  private static final char LIST_SEPARATOR = ',';
  private static final char LIST_END = ']';
  private static final char LIST_BEGIN = '[';

  private static final String STR_EXCEPTION_STACKTRACE = "'; Stacktrace is '";
  private static final String STR_EXCEPTION_MESSAGE = " exception occurred. Message is '";
  private static final String STR_EXCEPTION_END = "'";

  private static final String HOME_PREFIX = "~";
  private static final String USER_HOME_PROPERTY = "user.home";

  private static final double MILLIS = 0.000001;

  final public static void close(final AutoCloseable... resources) {
    for (final AutoCloseable res : resources) {
      if (res != null) {
        try {
          res.close();
        }
        catch (final Exception e) {
        }
      }
    }
  }

  final public static boolean isEmptyOrNull(final byte[] data) {
    return ((data == null) || (data.length == 0));
  }

  final public static void concatenate(final StringBuilder res, final Object... objs) {
    for (final Object o : objs) {
      if (o == null) {
        continue;
      }
      res.append(String.valueOf(o));
    }
  }

  final public static void writeBytes(final StringBuilder buf, final byte[] bytes) {
    buf.append(Helper.LIST_BEGIN);
    if (bytes != null) {
      for (int i = 0; i < bytes.length; i++) {
        if (i > 0) {
          buf.append(Helper.LIST_SEPARATOR);
        }
        buf.append(String.valueOf(bytes[i]));
      }
    }
    buf.append(Helper.LIST_END);
  }

  final public static void writeList(final StringBuilder buf, final List<? extends Object> objects) {
    Helper.writeList(buf, objects, Helper.DOUBLE_QUOTE, Helper.DOUBLE_QUOTE);
  }

  final public static void writeList(final StringBuilder buf, final List<? extends Object> objects, final String prefix, final String suffix) {
    buf.append(Helper.LIST_BEGIN);
    if (objects != null) {
      for (int i = 0; i < objects.size(); i++) {
        if (i > 0) {
          buf.append(Helper.LIST_SEPARATOR);
        }
        if (prefix != null) {
          buf.append(prefix);
        }
        buf.append(String.valueOf(objects.get(i)));
        if (suffix != null) {
          buf.append(suffix);
        }
      }
    }
    buf.append(Helper.LIST_END);
  }

  final public static void sleep(final int ms) {
    try {
      Thread.sleep(ms);
    }
    catch (final InterruptedException e) {
    }
  }

  final public static SocketAddress getServer(final String serverName) throws Exception {
    final int pos = serverName.indexOf(':');
    final String host = serverName.substring(0, pos);
    final int port = Integer.parseInt(serverName.substring(pos + 1, serverName.length()).trim());
    final SocketAddress server = new InetSocketAddress(host, port);
    return server;
  }

  final public static int getInt(final String val, final int defVal) {
    int result = defVal;
    try {
      if (LibStr.isNotEmptyOrNull(val)) {
        result = Integer.parseInt(val);
      }
    }
    catch (final NumberFormatException e) {
    }
    return result;
  }

  final public static long getLong(final String val, final long defVal) {
    long result = defVal;
    try {
      if (LibStr.isNotEmptyOrNull(val)) {
        result = Long.parseLong(val);
      }
    }
    catch (final NumberFormatException e) {
    }
    return result;
  }

  final public static double getDouble(final String val, final double defVal) {
    double result = defVal;
    try {
      if (LibStr.isNotEmptyOrNull(val)) {
        result = Double.parseDouble(val);
      }
    }
    catch (final NumberFormatException e) {
    }
    return result;
  }

  final public static boolean getBoolean(final String val, final boolean defVal) {
    boolean result = defVal;
    try {
      if (LibStr.isNotEmptyOrNull(val)) {
        result = Boolean.parseBoolean(val);
      }
    }
    catch (final Exception e) {
    }
    return result;
  }

  public static Date getDate(final String val, final DateFormat dateFormat, final Date defVal) {
    Date result = defVal;
    try {
      if (LibStr.isNotEmptyOrNull(val)) {
        result = dateFormat.parse(val);
      }
    }
    catch (final ParseException e) {
    }
    return result;
  }

  public static Date getDate(String val, final Date defDate, final SimpleDateFormat... formats) {
    if (LibStr.isEmptyOrNull(val)) { return defDate; }
    val = val.trim();
    Date result = defDate;
    for (final SimpleDateFormat format : formats) {
      try {
        result = format.parse(val);
        break;
      }
      catch (final ParseException e) {
      }
    }
    return result;
  }

  public static int size(final byte[] data) {
    return data != null ? data.length : 0;
  }

  final public static byte[] concatByteArrays(final byte[] a, final int startIdxA, final int lenA, final byte[] b, final int startIdxB, final int lenB) {
    final byte[] c = new byte[lenA + lenB];
    System.arraycopy(a, startIdxA, c, 0, lenA);
    System.arraycopy(b, startIdxB, c, lenA, lenB);
    return c;
  }

  final public static String getHomePath() {
    return System.getProperty(Helper.USER_HOME_PROPERTY).replace('\\', '/');
  }

  final public static Path getDirPath(final String pathStr) {
    return Helper.getPath(pathStr, true);
  }

  final public static Path getPath(String pathStr, final boolean createDir) {
    if (pathStr == null) { return null; }
    if (pathStr.startsWith(Helper.HOME_PREFIX)) {
      pathStr = Helper.getHomePath() + pathStr.substring(1);
    }
    Path path = Paths.get(pathStr);
    if (createDir) {
      try {
        final Path parent = path.getParent();
        if (parent != null) {
          Files.createDirectories(parent);
        }
      }
      catch (final IOException e) {
        path = null;
      }
    }
    return path;
  }

  public static Properties loadProperties(final String propertiesFile, final boolean sorted) throws IOException {
    InputStream inputStream = null;
    inputStream = new FileInputStream(propertiesFile);
    return Helper.loadProperties(inputStream, sorted);
  }

  public static Properties loadProperties(final InputStream inputStream, final boolean sorted) throws IOException {
    final Properties properties;
    if (sorted) {
      properties = new SortedProperties();
    }
    else {
      properties = new Properties();
    }
    try {
      properties.load(inputStream);
    }
    finally {
      Helper.close(inputStream);
    }
    return properties;
  }

  final public static String[] split(final String str, final boolean skipEmpty) {
    return Helper.split(str, Helper.SPLIT_SEPARATOR, skipEmpty);
  }

  final public static String[] split(final String str, final String expr, final boolean skipEmpty) {
    if (LibStr.isEmptyOrNull(str)) { return null; }
    String[] result = str.split(expr);
    if (skipEmpty) {
      final ArrayList<String> col = new ArrayList<>();
      for (final String element : result) {
        if (LibStr.isNotEmptyOrNull(element)) {
          col.add(element.trim());
        }
      }
      if (col.size() > 0) {
        result = new String[col.size()];
        col.toArray(result);
      }
      else {
        result = null;
      }
    }
    return result;
  }

  final public static double elapsed(final long startTime, final long endTime) {
    long elapsed = endTime - startTime;
    if (elapsed < 0) {
      elapsed = 0;
    }
    return elapsed * Helper.MILLIS;
  }

  public static String getExceptionAsString(final Throwable e) {
    return Helper.getExceptionAsString(e, false);
  }

  public static String getExceptionAsString(final Throwable e, final boolean stacktrace) {
    String msg = e.getMessage();
    if (msg == null) {
      msg = Helper.NO_DATA;
    }
    final StringBuilder sb = new StringBuilder(256);
    sb.append(e.getClass().getCanonicalName()).append(Helper.STR_EXCEPTION_MESSAGE).append(msg);
    if (stacktrace) {
      sb.append(Helper.STR_EXCEPTION_STACKTRACE).append(Helper.getStackTraceAsString(e));
    }
    sb.append(Helper.STR_EXCEPTION_END);
    return sb.toString();
  }

  public static String getStackTraceAsString(final Throwable e) {
    String returnString;
    final ByteArrayOutputStream ba = new ByteArrayOutputStream();
    try {
      e.printStackTrace(new PrintStream(ba, true, Helper.DEFAULT_ENCODING));
      returnString = ba.toString(Helper.DEFAULT_ENCODING);
    }
    catch (final UnsupportedEncodingException e1) {
      returnString = Helper.NO_DATA;
    }
    return returnString;
  }

  public static URL getResourceURL(final String resourceName, final Class<?> callingClass) {
    return Helper.getResourceURL(resourceName, callingClass, false);
  }

  public static URL getResourceURL(final String resourceName) {
    return Helper.getResourceURL(resourceName, null, false);
  }

  public static URL getResourceURL(final String resourceName, final Class<?> callingClass, final boolean last) {
    if (LibStr.isEmptyOrNull(resourceName)) { return null; }
    ClassLoader cl1 = null;
    ClassLoader cl2 = null;
    ClassLoader cl3 = null;
    URL url = null;
    if ((url == null) && (callingClass != null)) {
      cl1 = callingClass.getClassLoader();
      if (cl1 != null) {
        url = cl1.getResource(resourceName);
      }
    }
    if (url == null) {
      cl2 = Thread.currentThread().getContextClassLoader();
      if ((cl2 != null) && (cl2 != cl1)) {
        url = cl2.getResource(resourceName);
      }
    }
    if (url == null) {
      cl3 = Class.class.getClassLoader();
      if ((cl3 != null) && (cl3 != cl1) && (cl3 != cl2)) {
        url = cl3.getResource(resourceName);
      }
    }
    if ((url == null) && (resourceName.charAt(0) != '/') && !last) {
      url = Helper.getResourceURL('/' + resourceName, callingClass, true);
    }
    else if ((url == null) && (resourceName.charAt(0) == '/') && !last) {
      url = Helper.getResourceURL(resourceName.substring(1), callingClass, true);
    }
    return url;
  }

  public static void preCondition(final boolean condition, final String error) {
    if (!condition) { throw new RuntimeException(error); }
  }

  public static final String get(final String val, final String def) {
    return (val != null) ? val : def;
  }

}
