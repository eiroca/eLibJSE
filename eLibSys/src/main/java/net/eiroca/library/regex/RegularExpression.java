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
package net.eiroca.library.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.Logs;

public class RegularExpression {

  private static final String REG_EX_REPORTING = "RegEx Reporting";
  private static final int REGEX_DUMPTIME = 5 * 60;

  transient protected static final Logger logger = Logs.getLogger();
  transient protected static final Logger reporter = Logs.getLogger(REG_EX_REPORTING);

  private static List<ARegEx> rules = new ArrayList<>();

  static {
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {

      @Override
      public Thread newThread(final Runnable r) {
        final Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setName(REG_EX_REPORTING);
        t.setDaemon(true);
        return t;
      }

    });
    scheduler.scheduleAtFixedRate(new Runnable() {

      @Override
      public void run() {
        RegularExpression.dump();
      }

    }, RegularExpression.REGEX_DUMPTIME, RegularExpression.REGEX_DUMPTIME, TimeUnit.SECONDS);
  }

  public static ARegEx build(final String pattern, final int engine) {
    ARegEx result = null;
    try {
      if (engine == 1) {
        result = new RegExRE2J(pattern);
      }
      else {
        result = new RegExJava(pattern);
      }
      RegularExpression.rules.add(result);
    }
    catch (final Exception e) {
      System.err.println(e);
      RegularExpression.logger.warn("Invalid RegEx " + pattern, e);
    }
    return result;
  }

  public static List<ARegEx> getRules() {
    final List<ARegEx> result = new ArrayList<>();
    result.addAll(RegularExpression.rules);
    Collections.sort(result, new Comparator<ARegEx>() {

      @Override
      public int compare(final ARegEx r1, final ARegEx r2) {
        if (r1.totalTime < r2.totalTime) { return 1; }
        if (r1.totalTime > r2.totalTime) { return -1; }
        return r1.pattern.compareTo(r2.pattern);
      }

    });
    return result;
  }

  public static void dump() {
    if (RegularExpression.rules.size() == 0) { return; }
    final List<ARegEx> r = RegularExpression.getRules();
    long totalTime = 0;
    long totalCount = 0;
    RegularExpression.reporter.info(REG_EX_REPORTING);
    StringBuilder sb = new StringBuilder(128);
    for (final ARegEx e : r) {
      sb.setLength(0);
      totalCount += e.count;
      totalTime += e.totalTime;
      LibStr.encodeJava(sb, e.pattern);
      sb.append('\t').append(e.count).append('\t').append(e.totalTime / 1_000_000.0);
      RegularExpression.reporter.info(sb.toString());
    }
    RegularExpression.reporter.info("Total \t" + totalCount + "\t" + totalTime);
  }

}
