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

import java.text.SimpleDateFormat;
import java.util.Date;

public class LibDate {

  public static final SimpleDateFormat ISO8601_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  public static final SimpleDateFormat ISO8601_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  public static final SimpleDateFormat ISO8601_FULL = LibDate.ISO8601_2;

  public static final int ONE_HOUR = 3600 * 1000;
  public static final int ONE_DAY = 24 * 60 * 60 * 1000;

  /**
   * Get number of days between two dates
   *
   * @param first first date
   * @param second second date
   * @return number of days if first date less than second date, 0 if first date is bigger than
   *         second date, 1 if dates are the same
   */
  public static int getNumberOfDays(final Date first, final Date second) {
    final int compare = first.compareTo(second);
    if (compare > 0) {
      return 0;
    }
    else if (compare == 0) { return 1; }
    final long d1 = first.getTime();
    final long d2 = second.getTime();
    final int days = (int)(((d2 - d1) / LibDate.ONE_DAY) + 1);
    return days;
  }

}
