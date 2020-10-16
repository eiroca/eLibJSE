/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.library.diagnostics.actions;

import com.predic8.schema.Element;
import groovy.xml.QName;

public class WSElement extends Element {

  String name;
  QName qName;

  public WSElement(final String name, final QName qName) {
    this.name = name;
    this.qName = qName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public QName getType() {
    return qName;
  }

}
