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

import net.eiroca.library.metrics.datum.IDatum;

public class DatumCheck {

  public enum CheckViolation {
    OK, MIN, MAX, BOTH
  };

  private final String checkName;
  private final double weight;
  private final Double min;
  private final Double max;

  public DatumCheck(final String checkName, final double weight, final Double min, final Double max) {
    this.checkName = checkName;
    this.weight = weight;
    this.min = min;
    this.max = max;
  }

  public CheckViolation check(final IDatum d) {
    final double v = d.getValue();
    final boolean minKo = ((min != null) && (v < min));
    final boolean maxKo = ((max != null) && (v > max));
    if (minKo && maxKo) { return CheckViolation.BOTH; }
    if (minKo) { return CheckViolation.MIN; }
    if (maxKo) { return CheckViolation.MAX; }
    return CheckViolation.OK;
  }

  public Double getMin() {
    return min;
  }

  public Double getMax() {
    return max;
  }

  public String getCheckName() {
    return checkName;
  }

  public double getWeight() {
    return weight;
  }

}
