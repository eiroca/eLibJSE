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
package net.eiroca.library.metrics;

import java.text.MessageFormat;

public class MetricMetadata {

  private String name;
  private String displayFormat = "{0}";
  private double defValue = 0;
  //
  private String description;
  private String unit;
  private String rate;
  private String noagg;
  //
  private boolean calcDelta;
  private MetricAggregation aggregation = MetricAggregation.zero;

  public MetricMetadata(final String name) {
    this.name = name;
  }

  public MetricMetadata(final String name, final String displayFormat, final double defValue) {
    super();
    this.name = name;
    this.displayFormat = (displayFormat == null) ? "{0}" : displayFormat;
    this.defValue = defValue;
  }

  public String getInternalName() {
    return name;
  }

  public String getDisplayName() {
    return MessageFormat.format(displayFormat, name);
  }

  public double getDefValue() {
    return defValue;
  }

  public String getDisplayFormat() {
    return displayFormat;
  }

  public String getDescription() {
    return description;
  }

  public String getUnit() {
    return unit;
  }

  public String getRate() {
    return rate;
  }

  public String getNoagg() {
    return noagg;
  }

  public boolean getCalcDelta() {
    return calcDelta;
  }

  public MetricAggregation getAggregation() {
    return aggregation;
  }

  public MetricMetadata setName(final String name) {
    this.name = name;
    return this;
  }

  public MetricMetadata setDefValue(final double defValue) {
    this.defValue = defValue;
    return this;
  }

  public MetricMetadata setDisplayFormat(final String displayFormat) {
    this.displayFormat = displayFormat;
    return this;
  }

  public MetricMetadata setDescription(final String description) {
    this.description = description;
    return this;
  }

  public MetricMetadata setUnit(final String unit) {
    this.unit = unit;
    return this;
  }

  public MetricMetadata setRate(final String rate) {
    this.rate = rate;
    return this;
  }

  public MetricMetadata setNoagg(final String noagg) {
    this.noagg = noagg;
    return this;
  }

  public MetricMetadata setCalcDelta(final boolean calcDelta) {
    this.calcDelta = calcDelta;
    return this;
  }

  public MetricMetadata setAggregation(final MetricAggregation aggregation) {
    this.aggregation = aggregation;
    return this;
  }

}
