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
package net.eiroca.library.diagnostics.actiondata;

import java.text.DecimalFormat;

public class Messages {

  public static final String RAWPREFIX = "_";

  public static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.##");

  public static final String STRING_SEVERE = "Severe";
  public static final String STRING_WARNING = "Warning";
  public static final String STRING_INFORMATIONAL = "Informational";

  public static final String FMT_MEASURED_VALUE_WAS_HIGHER_THAN_THRESHOLD_EMAIL_TEXT = "Was {0} but should be higher than {1}.";
  public static final String FMT_MEASURED_VALUE_WAS_LOWER_THAN_THRESHOLD_EMAIL_TEXT = "Was {0} but should be lower than {1}.";

}
