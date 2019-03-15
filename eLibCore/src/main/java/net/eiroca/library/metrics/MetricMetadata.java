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

  public void setName(final String name) {
    this.name = name;
  }

  public double getDefValue() {
    return defValue;
  }

  public void setDefValue(final double defValue) {
    this.defValue = defValue;
  }

  public String getDisplayFormat() {
    return displayFormat;
  }

  public void setDisplayFormat(String displayFormat) {
    this.displayFormat = displayFormat;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getRate() {
    return rate;
  }

  public void setRate(String rate) {
    this.rate = rate;
  }

  public String getNoagg() {
    return noagg;
  }

  public void setNoagg(String noagg) {
    this.noagg = noagg;
  }

  public boolean getCalcDelta() {
    return calcDelta;
  }

  
  public void setCalcDelta(boolean calcDelta) {
    this.calcDelta = calcDelta;
  }

}
