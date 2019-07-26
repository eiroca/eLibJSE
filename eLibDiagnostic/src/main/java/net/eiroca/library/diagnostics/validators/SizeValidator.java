/**
 *
 * Copyright (C) 1999-2019 Enrico Croce - AGPL >= 3.0
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
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.system.IContext;

public class SizeValidator extends Validator {

  private static final String CONFIG_COMPARE_BYTES = "compareBytes";
  long compareBytes;

  @Override
  public void setup(final IContext context) throws CommandException {
    compareBytes = context.getConfigLong(SizeValidator.CONFIG_COMPARE_BYTES, 0);
  }

  @Override
  public boolean isValid(final String content) throws CommandException {
    final int size = content.getBytes().length;
    final boolean verified = (size == compareBytes);
    if (!verified && !silent) {
      CommandException.Invalid(MessageFormat.format("Expected {0} bytes, but was {1} bytes.", compareBytes, size));
    }
    return verified;
  }

}
