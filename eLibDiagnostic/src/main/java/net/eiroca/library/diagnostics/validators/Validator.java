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

import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.ICommand;
import net.eiroca.library.diagnostics.IValidator;
import net.eiroca.library.system.IContext;

public class Validator implements ICommand, IValidator<String> {

  protected boolean silent = true;

  @Override
  public void setup(final IContext context) throws CommandException {
  }

  @Override
  public boolean isValid(final String content) throws CommandException {
    return true;
  }

  @Override
  public void close() throws Exception {
  }

}
