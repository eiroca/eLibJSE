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

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.system.IContext;

public class RegExValidator extends Validator {

  private static final String CONFIG_REGEX = "regex";

  Pattern regex;

  @Override
  public void setup(final IContext context) throws CommandException {
    super.setup(context);
    final String regexString = context.getConfigString(RegExValidator.CONFIG_REGEX, ".*");
    regex = Pattern.compile(regexString, Pattern.DOTALL);
  }

  @Override
  public boolean isValid(final String content) throws CommandException {
    final Matcher m = regex.matcher(content);
    final boolean verified = m.matches();
    if (!verified && !silent) {
      CommandException.Invalid(MessageFormat.format("Expected string \"{0}\" didn't match RegEx {1}.", content, regex.pattern()));
    }
    return verified;
  }

}
