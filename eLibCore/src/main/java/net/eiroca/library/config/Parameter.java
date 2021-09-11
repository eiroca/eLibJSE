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
package net.eiroca.library.config;

import net.eiroca.library.core.LibStr;

public abstract class Parameter<T extends Object> {

  protected Parameters owner;
  protected String internalName;

  protected String name;
  protected boolean required;
  protected boolean nullable;
  private T defValue;

  protected String label;

  protected String description;

  public Parameter(final Parameters owner, final String paramName) {
    this(owner, paramName, null, true, false);
  }

  public Parameter(final Parameters owner, final String paramName, final T defValue) {
    this(owner, paramName, defValue, false, defValue == null ? true : false);
  }

  public Parameter(final Parameters owner, final String paramName, final T defValue, final boolean required, final boolean nullable) {
    setName(paramName);
    this.required = required;
    this.nullable = nullable;
    this.defValue = defValue;
    this.owner = owner;
    owner.add(this);
  }

  abstract public boolean isValid(Object value);

  public T convertString(final String strValue) {
    return null;
  }

  public String encodeString(final Object val) {
    return String.valueOf(val);
  }

  @SuppressWarnings("unchecked")
  public T get() {
    final T value = (T)owner.getValue(this);
    return value;
  }

  public void set(final T val) {
    owner.setValue(this, val);
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

  public String getInternalName() {
    if (internalName == null) {
      internalName = LibStr.getVarName(name);
    }
    return internalName;
  }

  public void setInternalName(final String internalName) {
    this.internalName = internalName;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append("=");
    final T v = get();
    if (v == null) {
      sb.append("_NULL_");
    }
    else {
      sb.append(v);
    }
    return sb.toString();
  }

  public String getLabel() {
    return label;
  }

  public Parameter<T> setLabel(final String label) {
    this.label = label;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Parameter<T> setDescription(final String description) {
    this.description = description;
    return this;
  }

}
