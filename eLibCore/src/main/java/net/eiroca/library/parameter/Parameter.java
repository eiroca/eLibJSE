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

public abstract class Parameter<T> {

  private String name;
  private boolean required;
  private boolean nullable;
  protected T value;
  protected T defValue;

  public Parameter(final Parameters owner, final String paramName, final T defValue, final boolean required, final boolean nullable) {
    this.name = paramName;
    this.required = required;
    this.nullable = nullable;
    this.value = defValue;
    this.defValue = defValue;
    owner.add(this);
  }

  public Parameter(final Parameters owner, final String paramName, final T defValue) {
    this(owner, paramName, defValue, false, defValue == null ? true : false);
  }

  public Parameter(final Parameters owner, final String paramName) {
    this(owner, paramName, null, true, false);
  }

  abstract public void formString(String strValue);

  public T get() {
    return value;
  }

  public void set(final T val) {
    value = val;
  }

  public String getName() {
    return name;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(final boolean required) {
    this.required = required;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(final boolean nullable) {
    this.nullable = nullable;
  }

  public T getDefault() {
    return defValue;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append("=");
    sb.append(value == null ? defValue : value);
    return sb.toString();
  }
}
