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
package net.eiroca.library.sysadm.monitoring.api;

import net.eiroca.ext.library.gson.SimpleGson;

public class Event {

  private final long timestamp;
  private final SimpleGson data;
  private final EventRule rule;

  public Event(final long timeStamp, final SimpleGson data, final EventRule rule) {
    timestamp = timeStamp;
    this.data = data;
    this.rule = rule;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public SimpleGson getData() {
    return data;
  }

  public EventRule getRule() {
    return rule;
  }

}
