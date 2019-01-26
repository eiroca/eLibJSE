/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.library.parameter;

import net.eiroca.library.core.LibStr;

public class DoubleParameter extends Parameter<Double> {

  public DoubleParameter(final Parameters owner, final String paramName, final double paramDef) {
    super(owner, paramName, paramDef);
  }

  public DoubleParameter(final Parameters owner, final String paramName) {
    super(owner, paramName);
  }

  @Override
  public void formString(final String value) {
    if (LibStr.isEmptyOrNull(value)) {
      this.value = defValue;
    }
    else {
      try {
        this.value = new Double(value.trim());
      }
      catch (final NumberFormatException e) {
        this.value = defValue;
      }
    }
  }

}
