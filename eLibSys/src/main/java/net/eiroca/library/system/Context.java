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
package net.eiroca.library.system;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;

public class Context implements IContext {

  private static final ThreadLocal<Context> currentContext = new ThreadLocal<Context>() {

    @Override
    protected Context initialValue() {
      final Context c = new Context(Thread.currentThread().getName());
      for (final Entry<Object, Object> e : System.getProperties().entrySet()) {
        final Object key = e.getKey();
        final Object val = e.getValue();
        if ((val != null) && (key != null)) {
          c.config.put(key.toString(), val.toString());
        }
      }
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

  public Context(final String name) {
    this.name = name;
    logger = LoggerFactory.getLogger(name);
  }

  // ILog

  private LogLevel getLogLevel(final LogLevel level) {
    int importance = level.ordinal() + bonusLevel;
    if (importance < 0) {
      importance = 0;
    }
    if (importance > 4) {
      importance = 4;
    }
    return LogLevel.values()[importance];
  }

  @Override
  public boolean isLoggable(final LogLevel level) {
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
        return logger.isErrorEnabled();
    }
    return false;
  }

  @Override
  public void trace(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.trace);
    if (isLoggable(l)) {
      log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void debug(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.debug);
    if (isLoggable(l)) {
      log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void info(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.info);
    if (isLoggable(l)) {
      log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void warn(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.warn);
    if (isLoggable(l)) {
      log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void error(final Object... msg) {
    final LogLevel l = getLogLevel(LogLevel.error);
    if (isLoggable(l)) {
      log(l, LibStr.concatenate(msg));
    }
  }

  @Override
  public void log(final LogLevel priority, final String msg) {
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
        logger.error(msg);
        break;
    }
  }

  @Override
  public void logF(final LogLevel priority, final String format, final Object... args) {
    final LogLevel l = getLogLevel(priority);
    if (isLoggable(l)) {
      log(l, MessageFormat.format(format, args));
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
