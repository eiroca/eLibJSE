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
package net.eiroca.library.sysadm.monitoring.api;

import net.eiroca.ext.library.gson.SimpleGson;
import net.eiroca.library.metrics.MetricMetadata;

public class Event {

  private final long timestamp;
  private final MetricMetadata metricInfo;
  private final SimpleGson data;
  private final EventRule rule;
  private final double value;

  public Event(final long timeStamp, final SimpleGson data, final MetricMetadata metricInfo, final double value, final EventRule rule) {
    timestamp = timeStamp;
    this.metricInfo = metricInfo;
    this.data = data;
    this.value = value;
    this.rule = rule;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public MetricMetadata getMetricInfo() {
    return metricInfo;
  }

  public SimpleGson getData() {
    return data;
  }

  public double getValue() {
    return value;
  }

  public EventRule getRule() {
    return rule;
  }

}
