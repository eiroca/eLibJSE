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
package net.eiroca.library.diagnostics.validators;

import java.util.HashMap;
import java.util.Map;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.system.IContext;

public class GenericValidator extends Validator {

  private static final String CONFIG_MATCH_CONTENT = "matchContent";

  private static final Map<String, Class<? extends Validator>> VALID_RULES = new HashMap<>();
  static {
    GenericValidator.VALID_RULES.put("Success if contains", ContainValidator.class);
    GenericValidator.VALID_RULES.put("Error if contains", ContainNotValidator.class);
    GenericValidator.VALID_RULES.put("Regular Expression", RegExValidator.class);
    GenericValidator.VALID_RULES.put("Expected size in bytes", SizeValidator.class);
    GenericValidator.VALID_RULES.put("contains", ContainValidator.class);
    GenericValidator.VALID_RULES.put("notContains", ContainNotValidator.class);
    GenericValidator.VALID_RULES.put("regex", RegExValidator.class);
    GenericValidator.VALID_RULES.put("size", SizeValidator.class);
  }

  Validator rule;

  @Override
  public void setup(final IContext context) throws CommandException {
    final String matchContentStr = context.getConfigString(GenericValidator.CONFIG_MATCH_CONTENT, "OFF");
    if (GenericValidator.VALID_RULES.containsKey(matchContentStr)) {
      try {
        rule = GenericValidator.VALID_RULES.get(matchContentStr).newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        rule = new Validator();
      }
    }
    else {
      rule = new Validator();
    }
    rule.setup(context);
  }

  @Override
  public boolean isValid(final String content) throws CommandException {
    if (content == null) { return true; }
    return rule.isValid(content);
  }

}
