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
package net.eiroca.library.util;

import java.util.Iterator;
import javax.servlet.ServletConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.eiroca.library.util.impl.BaseContext;

public class Application extends BaseContext {

  private static final String CONTEXT_NAME = "me";
  private static final String LOGID_ERRORS = "Log.Errors";
  private static final String LOGID_EVENTS = "Log.Events";

  public static final String PROPERTIESPATH = "Properties.Path";
  public static final String PROPERTIESTIMEOUT = "Properties.TimeOut";

  public static final Logger logEvents = LoggerFactory.getLogger(Application.LOGID_EVENTS);
  public static final Logger logErrors = LoggerFactory.getLogger(Application.LOGID_ERRORS);

  private static Application instance;

  public synchronized static final Application getInstance() {
    if (Application.instance == null) {
      Application.instance = new Application();
    }
    return Application.instance;
  }

  /** Properties section * */

  private long lastLoad = 0;

  private ResourceLocator propSrc = null;
  private boolean updating = false;

  private Application() {
    super(null, Application.CONTEXT_NAME);
    declare(Application.PROPERTIESPATH, "config.xml");
    declare(Application.PROPERTIESTIMEOUT, "60000");
  }

  public void checkConf() {
    boolean needUpdate = false;
    if (propSrc == null) {
      needUpdate = true;
    }
    else {
      final int timeOut = getInt(Application.PROPERTIESTIMEOUT);
      final long now = System.currentTimeMillis() - timeOut;
      if (lastLoad < now) {
        final long lastMod = propSrc.lastModified();
        if (lastMod < 1) {
          needUpdate = true;
        }
        else {
          needUpdate = (lastMod > lastLoad);
        }
      }
    }
    if ((needUpdate) && (!updating)) {
      reloadProperties();
    }
  }

  public final synchronized void reloadProperties() {
    updating = true;
    try {
      final String path = getString(Application.PROPERTIESPATH);
      try {
        propSrc = new ResourceLocator(path);
        laodProperties(propSrc);
        lastLoad = System.currentTimeMillis();
      }
      catch (final Exception e) {
        fatal("Error Reading Properties (" + path + ")", e);
      }
      success("Config.reloadProperties()");
    }
    finally {
      updating = false;
    }
  }

  public void updateConfig(final ServletConfig conf) {
    if (conf != null) {
      final Iterator<String> it = propDef.keySet().iterator();
      while (it.hasNext()) {
        final String nam = it.next();
        final String val = conf.getInitParameter(nam);
        if (val != null) {
          propVal.put(nam, val);
        }
      }
      reloadProperties();
    }
  }

}
