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

import java.util.Map;

public class LookupRule extends Rule {

  public Map<String, Map<String, String>> table;

  public LookupRule() {
    super();
  }

  public Map<String, String> lookup(final String key) {
    final long startTime = System.currentTimeMillis();
    count++;
    final Map<String, String> result = table.get(key);
    hits += result != null ? 1 : 0;
    elapsed += System.currentTimeMillis() - startTime;
    return result;
  }

}
