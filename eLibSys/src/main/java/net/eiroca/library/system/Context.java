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
package net.eiroca.library.system;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;

public class Context implements IContext {

  private static final ThreadLocal<Context> currentContext = new ThreadLocal<Context>() {

    @Override
    protected Context initialValue() {
      final Context c = new Context(Thread.currentThread().getName());
      Context.importProperties(c, System.getProperties());
      return c;
    }
  };

  protected String name;
  protected Logger logger;
  protected int bonusLevel = 0;

  protected Map<String, String> config = new HashMap<>();
  protected Map<String, Object> attributes = new HashMap<>();

  public static Context current() {
    return Context.currentContext.get();
  }

  public static void importProperties(final Context c, final Properties properties) {
    for (final Entry<Object, Object> e : properties.entrySet()) {
      final Object key = e.getKey();
      final Object val = e.getValue();
      if ((val != null) && (key != null)) {
        c.config.put(key.toString(), val.toString());
      }
    }
  }

  public Context(final String name) {
    this.name = name;
    logger = LoggerFactory.getLogger(name);
  }

  public Context(final String name, final Properties properties) {
    this(name);
    Context.importProperties(this, properties);
  }

  final private LogLevel getLogLevel(final LogLevel level) {
    int importance = level.ordinal() + bonusLevel;
    if (importance < 0) {
      importance = 0;
    }
    if (importance >= LogLevel.values().length) {
      importance = LogLevel.values().length - 1;
    }
    return LogLevel.values()[importance];
  }

  final private boolean internalIsLoggable(final LogLevel level) {
    switch (level) {
      case trace:
        return logger.isTraceEnabled();
      case debug:
        return logger.isDebugEnabled();
      case info:
        return logger.isInfoEnabled();
      case warn:
        return logger.isWarnEnabled();
      case error:
      case fatal:
        return logger.isErrorEnabled();
    }
    return false;
  }

  final private void internalLog(final LogLevel priority, final String msg) {
    switch (priority) {
      case trace:
        logger.trace(msg);
        break;
      case debug:
        logger.debug(msg);
        break;
      case info:
        logger.info(msg);
        break;
      case warn:
        logger.warn(msg);
        break;
      case error:
      case fatal:
        logger.error(msg);
        break;
    }
  }

  // ILog

  @Override
  public boolean isLoggable(final LogLevel priority) {
    final LogLevel l = getLogLevel(priority);
    return internalIsLoggable(l);
  }

  @Override
  public void trace(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.trace);
    if (internalIsLoggable(l)) {
      internalLog(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void debug(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.debug);
    if (internalIsLoggable(l)) {
      internalLog(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void info(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.info);
    if (internalIsLoggable(l)) {
      internalLog(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void warn(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.warn);
    if (internalIsLoggable(l)) {
      internalLog(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void error(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.error);
    if (internalIsLoggable(l)) {
      internalLog(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void fatal(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.fatal);
    if (internalIsLoggable(l)) {
      log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void log(final LogLevel priority, final String msg) {
    final LogLevel l = getLogLevel(priority);
    if (internalIsLoggable(l)) {
      internalLog(l, msg);
    }
  }

  @Override
  public void logF(final LogLevel priority, final String format, final Object... args) {
    final LogLevel l = getLogLevel(priority);
    if (internalIsLoggable(l)) {
      internalLog(l, MessageFormat.format(format, args));
    }
  }

  // IConfig

  @Override
  public int getConfigInt(final String propName, final int defValue) {
    final String result = config.get(propName);
    return Helper.getInt(result, defValue);
  }

  @Override
  public String getConfigString(final String propName, final String defValue) {
    final String result = config.get(propName);
    return result != null ? result : defValue;
  }

  @Override
  public long getConfigLong(final String propName, final long defValue) {
    final String result = config.get(propName);
    return Helper.getLong(result, defValue);
  }

  @Override
  public boolean getConfigBoolean(final String propName, final boolean defValue) {
    final String result = config.get(propName);
    return Helper.getBoolean(result, defValue);
  }

  @Override
  public String getConfigPassword(final String propName) {
    final String result = config.get(propName);
    return result;
  }

  @Override
  public File getConfigFile(final String propName) {
    final String result = config.get(propName);
    return result != null ? new File(result) : null;
  }

  @Override
  public URL getConfigUrl(final String propName) {
    final String result = config.get(propName);
    try {
      return result != null ? new URL(result) : null;
    }
    catch (final MalformedURLException e) {
      return null;
    }
  }

  @Override
  public boolean hasConfig(final String key) {
    return config.containsKey(key);
  }

  // ---

  public void setConfig(final String name, final String val) {
    config.put(name, val);
  }

  public void setAttribute(final String name, final Object val) {
    attributes.put(name, val);
  }

  public Object getAttribute(final String name) {
    return attributes.get(name);
  }

}
