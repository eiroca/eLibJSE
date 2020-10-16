/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.util.impl;

import java.util.HashMap;
import java.util.Iterator;
import net.eiroca.library.util.Application;
import net.eiroca.library.util.Context;
import net.eiroca.library.util.Counter;
import net.eiroca.library.util.ResourceLocator;

public class BaseContext implements Context {

  private static final String STATE_KO = " enters in 'error' condition";
  private static final String STATE_OK = " enters in 'working' condition";

  Context parent;
  String name;

  /* CONTEXT */
  private final HashMap<String, Context> contexts = new HashMap<>();

  /* COUNTER */
  private static final String ERROR_PREFIX = "Error.";

  private final HashMap<String, BaseCounter> counters = new HashMap<>();

  /* CONFIGURATION */
  protected HashMap<String, String> propDef = new HashMap<>();
  protected HashMap<String, String> propVal = new HashMap<>();

  public BaseContext(final Context parent, final String name) {
    this.parent = parent;
    this.name = name;
  }

  @Override
  public final void declare(final String propName, final String propDefault) {
    propVal.put(propName, propDefault);
    propDef.put(propName, propDefault);
  }

  public void enterStatusKO() {
    Application.logErrors.error(name + BaseContext.STATE_KO);
  }

  public void enterStatusOK() {
    Application.logErrors.error(name + BaseContext.STATE_OK);
  }

  /**
   * Writes an error message. The message is written in Log.Events and in Log.Errors. The counter
   * "Errors.<who>" is also increased (with failure).
   * @param why The reason of the log (must be not null)
   * @param err Optional associated error (can be null)
   */
  @Override
  public void error(final String why, final Exception err) {
    touch(true);
    final String msg = getMsg(why).toString();
    Application.logEvents.error(msg, err);
    Application.logErrors.warn(msg, err);
  }

  /**
   * Writes a fatal message. The message is written in Log.Events and in Log.Errors. The counter
   * "Errors.<who>" is also increased (with failure).
   * @param why The reason of the log (must be not null)
   * @param err Optional associated error (can be null)
   */
  @Override
  public void fatal(final String why, final Exception err) {
    touch(true);
    final String msg = getMsg(why).toString();
    Application.logEvents.error(msg, err);
    Application.logErrors.error(msg, err);
  }

  /**
   * Returns a copy of the contexts
   * @return An hashmap with the (name, context value) pairs
   */
  public HashMap<String, Context> getContexts() {
    final HashMap<String, Context> result = new HashMap<>();
    final Iterator<String> it = contexts.keySet().iterator();
    while (it.hasNext()) {
      final String nam = it.next();
      result.put(nam, contexts.get(nam));
    }
    return result;
  }

  @Override
  public Counter getCounter(final String name) {
    BaseCounter inf;
    synchronized (counters) {
      inf = counters.get(name);
      if (inf == null) {
        inf = new BaseCounter(-1, -1);
        counters.put(name, inf);
      }
    }
    return inf;
  }

  /**
   * Returns a copy of the counters
   * @return An hashmap with the (name, counter value) pairs
   */
  public HashMap<String, BaseCounter> getCounters() {
    final HashMap<String, BaseCounter> result = new HashMap<>();
    final Iterator<String> it = counters.keySet().iterator();
    while (it.hasNext()) {
      final String nam = it.next();
      result.put(nam, counters.get(nam));
    }
    return result;
  }

  @Override
  public String getFullName() {
    String fullName;
    if (parent != null) {
      fullName = parent.getFullName() + "." + name;
    }
    else {
      fullName = name;
    }
    return fullName;
  }

  @Override
  public int getInt(final String propName) {
    final String def = propDef.get(propName);
    if (def == null) {
      error("Invalid properity request (" + propName + ")", null);
      return 0;
    }
    try {
      return Integer.parseInt(propVal.get(propName));
    }
    catch (final Exception e) {
      return Integer.parseInt(def);
    }
  }

  /* LOGGING */
  /**
   * Returns a human readable string with the given informations
   * @param why If not null is added to the output string
   * @return A StringBuffer with the result
   */
  private final StringBuffer getMsg(final String why) {
    final StringBuffer msg = new StringBuffer(128);
    msg.append(name);
    if (why != null) {
      msg.append('.');
      msg.append(why);
    }
    return msg;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Context getParent() {
    return parent;
  }

  public HashMap<String, String> getProperties() {
    final HashMap<String, String> result = new HashMap<>();
    final Iterator<String> it = propDef.keySet().iterator();
    while (it.hasNext()) {
      final String nam = it.next();
      result.put(nam, getString(nam));
    }
    return result;
  }

  @Override
  public String getString(final String propName) {
    final String def = propDef.get(propName);
    if (def == null) {
      error("Invalid properity request (" + propName + ")", null);
      return null;
    }
    final String val = propVal.get(propName);
    return (val == null ? def : val);
  }

  @Override
  public Context getSubContext(final String aName) {
    Context c;
    synchronized (contexts) {
      c = contexts.get(aName);
      if (c == null) {
        c = new BaseContext(this, aName);
        contexts.put(aName, c);
      }
    }
    return c;
  }

  public void laodProperties(final ResourceLocator propSrc) {
    // TODO
  }

  /**
   * Resets all the counters
   */
  public final void resetCounters() {
    final Iterator<String> i = counters.keySet().iterator();
    BaseCounter inf;
    while (i.hasNext()) {
      inf = counters.get(i.next());
      inf.reset();
    }
  }

  /**
   * Writes a success message. The message is written in Log.Events. The counter "Errors.<who>" is
   * also increased (with success).
   * @param who The originator, if null defWho is used
   * @param why The reason of the log (must be not null)
   */
  @Override
  public void success(final String why) {
    touch(false);
    Application.logEvents.info(getMsg(why).toString());
  }

  /**
   * The methods tracks the "touch" events on a particular counter identified by "who". The "touch"
   * can be originated by an error or by a success. The touch can generte a "state" change of the
   * associated counter (e. g. if too much errors are happened), this kind of events are intercepted
   * and logged in the "Log.Errors".
   * @param who Counter to be used
   * @param err If true the touch is due to an error
   */
  public void touch(final boolean err) {
    final Counter inf = touch(this, err);
    final int ls = inf.getLastStatus();
    final int ns = inf.getStatus();
    if (ls != ns) {
      switch (ns) {
        case (BaseCounter.ST_OK): {
          enterStatusOK();
          break;
        }
        case (BaseCounter.ST_ERROR): {
          enterStatusKO();
          break;
        }
      }
    }
  }

  public Counter touch(final Context c, final boolean err) {
    return touch(BaseContext.ERROR_PREFIX + c.getName(), err);
  }

  public Counter touch(final String name, final boolean err) {
    final Counter inf = getCounter(name);
    inf.touch(err);
    return inf;
  }

  /**
   * Writes a warning message. The message is written in Log.Events.
   * @param why The reason of the log (must be not null)
   * @param err Optional associated error (can be null)
   */
  @Override
  public void warning(final String why, final Exception err) {
    Application.logEvents.warn(getMsg(why).toString(), err);
  }

}
