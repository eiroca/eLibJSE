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
package net.eiroca.library.rule.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.eiroca.library.rule.Rule;

public abstract class RuleGroup {

  private final String name;
  public Set<String> missingKey = new HashSet<>();

  public String getContextName() {
    return name;
  }

  public RuleGroup(final String name) {
    this.name = name;
  }

  abstract public Collection<? extends Rule> getRules();

}
