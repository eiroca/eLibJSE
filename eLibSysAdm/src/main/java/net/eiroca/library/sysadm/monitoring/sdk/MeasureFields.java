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
package net.eiroca.library.sysadm.monitoring.sdk;

public class MeasureFields {

  public static final String FLD_DATETIME = "@timestamp";
  public static final String FLD_METRIC = "metric";
  public static final String FLD_VALUE = "value";

  public static final String FLD_GROUP = "group";
  public static final String FLD_SPLIT = "split";
  public static final String FLD_SPLIT_GROUP = "splitGroup";
  public static final String FLD_SPLIT_NAME = "splitName";

  public static final String FLD_HOST = "host";
  public static final String FLD_SOURCE = "source";
  public static final String FLD_TAGS = "tags[]";
  public static final String FLD_TAGS_ALT = "tags";

  public static final String FLD_STATUS = "status";
  public static final String FLD_STATUS_DESC = "violation";
  public static final String FLD_STATUS_MINVAL = "minval";
  public static final String FLD_STATUS_MAXVAL = "maxval";

}
