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

public class MeasureMetadata {

  private String name;
  private String nameFormat = null;
  private double defValue;

  public MeasureMetadata(final String name, final String nameFormat, final double defValue) {
    super();
    this.name = name;
    this.nameFormat = nameFormat;
    this.defValue = defValue;
  }

  public String getName() {
    return (nameFormat == null) ? name : MessageFormat.format(nameFormat, name);
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

}
