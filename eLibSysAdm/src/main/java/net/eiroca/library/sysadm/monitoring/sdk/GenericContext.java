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
package net.eiroca.library.sysadm.monitoring.sdk;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.IContext;

public class GenericContext implements IContext {

  protected static final Level[] LOGLEVELS = new Level[] {
      Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR
  };

  protected Logger logger;
  protected String name;
  protected String runner;
  protected Properties config;

  public GenericContext(final String contextName, final Properties config) {
    name = contextName;
    runner = Thread.currentThread().getName();
    logger = LoggerFactory.getLogger(contextName);
    this.config = config;
  }

  public String getConfigString(final String propName) {
    return config.getProperty(propName);
  }

  public String getRunner() {
    return runner;
  }

  private static final Level getLevel(final LogLevel level) {
    int importance = level.ordinal();
    if (importance < 0) {
      importance = 0;
    }
    if (importance >= GenericContext.LOGLEVELS.length) {
      importance = GenericContext.LOGLEVELS.length - 1;
    }
    return GenericContext.LOGLEVELS[importance];
  }

  // IConfig

  @Override
  public int getConfigInt(final String propName, final int defValue) {
    final String val = getConfigString(propName);
    return val != null ? Helper.getInt(val, defValue) : defValue;
  }

  @Override
  public String getConfigString(final String propName, final String defValue) {
    final String val = getConfigString(propName);
    return val != null ? val : defValue;
  }

  @Override
  public long getConfigLong(final String propName, final long defValue) {
    final String val = getConfigString(propName);
    return val != null ? Helper.getLong(val, defValue) : defValue;
  }

  @Override
  public boolean getConfigBoolean(final String propName, final boolean defValue) {
    final String val = getConfigString(propName);
    return val != null ? Helper.getBoolean(val, defValue) : defValue;
  }

  @Override
  public String getConfigPassword(final String propName) {
    final String val = getConfigString(propName);
    return val != null ? val : null;
  }

  @Override
  public File getConfigFile(final String propName) {
    final String val = getConfigString(propName);
    return val != null ? new File(val) : null;
  }

  @Override
  public URL getConfigUrl(final String propName) {
    final String val = getConfigString(propName);
    try {
      return val != null ? new URL(val) : null;
    }
    catch (final MalformedURLException e) {
      return null;
    }
  }

  // ILog

  @Override
  public boolean isLoggable(final LogLevel level) {
    return true;
  }

  @Override
  public void trace(final Object... msg) {
    if (logger.isTraceEnabled()) {
      logger.trace(LibStr.concatenate(msg));
    }
  }

  @Override
  public void debug(final Object... msg) {
    if (logger.isDebugEnabled()) {
      logger.debug(LibStr.concatenate(msg));
    }
  }

  @Override
  public void info(final Object... msg) {
    if (logger.isInfoEnabled()) {
      logger.info(LibStr.concatenate(msg));
    }
  }

  @Override
  public void warn(final Object... msg) {
    if (logger.isWarnEnabled()) {
      logger.warn(LibStr.concatenate(msg));
    }
  }

  @Override
  public void error(final Object... msg) {
    if (logger.isErrorEnabled()) {
      logger.error(LibStr.concatenate(msg));
    }
  }

  @Override
  public void log(final LogLevel priority, final String msg) {
    switch (GenericContext.getLevel(priority)) {
      case TRACE:
        logger.trace(msg);
        break;
      case DEBUG:
        logger.debug(msg);
        break;
      case WARN:
        logger.warn(msg);
        break;
      case ERROR:
        logger.error(msg);
        break;
      default:
        logger.info(msg);
        break;
    }
  }

  @Override
  public void logF(final LogLevel priority, final String format, final Object... args) {
    switch (GenericContext.getLevel(priority)) {
      case TRACE:
        if (logger.isTraceEnabled()) {
          logger.trace(MessageFormat.format(format, args));
        }
        break;
      case DEBUG:
        if (logger.isDebugEnabled()) {
          logger.debug(MessageFormat.format(format, args));
        }
        break;
      case WARN:
        if (logger.isWarnEnabled()) {
          logger.warn(MessageFormat.format(format, args));
        }
        break;
      case ERROR:
        if (logger.isErrorEnabled()) {
          logger.error(MessageFormat.format(format, args));
        }
        break;
      default:
        if (logger.isInfoEnabled()) {
          logger.info(MessageFormat.format(format, args));
        }
        break;
    }
  }

}
