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
package net.eiroca.library.rule;

public class GenericRule extends Rule {

  private final String[] filter;

  private final String[] result;

  public GenericRule(final String name, final String[] filter, final String[] result) {
    super();
    this.filter = filter;
    this.result = result;
    this.name = name;
  }

  public String[] match(final String[] data) {
    final long startTime = System.currentTimeMillis();
    final String[] result = this.result;
    count++;
    hits += (result != null) ? 1 : 0;
    elapsed += System.currentTimeMillis() - startTime;
    return result;
  }

  public String[] getFilter() {
    return filter;
  }

  public String[] getResult() {
    return result;
  }

}
