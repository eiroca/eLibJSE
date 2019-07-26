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
package net.eiroca.library.diagnostics.actions;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.text.StringSubstitutor;
import net.eiroca.library.config.parameter.BooleanParameter;
import net.eiroca.library.config.parameter.LongParameter;
import net.eiroca.library.config.parameter.PathParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.csv.CSVMap;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.system.IContext;

public abstract class CommandAction extends BaseAction {

  private static final String CSV_ENCODING = "UTF-8";
  private static final char CSV_COMMENT = '#';
  private static final char CSV_SEPARATOR = '\t';
  private static final char CSV_QUOTE = '"';

  private static final String COMMAND_IS_NULL = "The 'command' parameter is null or empty.";
  private static final String COMMANDKEY_IS_NULL = "The 'commandKey' parameter is null or empty.";
  private static final String MISSINGKEY_IS_NULL = "The 'missingKey' parameter is null or empty.";
  private static final String COMMANDTABLE_IS_NULL = "The 'commandFile' parameter is null or invalid.";
  protected static final String COMMAND_IS_INVALID = "The command is null or empty.";

  protected final BooleanParameter pIsCommandKey = new BooleanParameter(params, "isCommandKey", false, true, false);
  protected final StringParameter pCommand = new StringParameter(params, "command", null, false, true);
  protected final StringParameter pCommandKey = new StringParameter(params, "commandKey", null, false, true);
  protected final StringParameter pMissingKey = new StringParameter(params, "missingKey", null, false, true);
  protected final PathParameter pCommandTable = new PathParameter(params, "commandFile", null, false, true);

  protected final LongParameter pOutputBufferSize = new LongParameter(params, "bufferSize", 32 * 1024, true, false);
  protected final BooleanParameter pCapture = new BooleanParameter(params, "capture", true, true, false);
  CSVMap commandMap = null;

  @Override
  public void setup(final IContext context) throws CommandException {
    super.setup(context);
    if (pIsCommandKey.get()) {
      if (LibStr.isEmptyOrNull(pCommandKey.get())) {
        CommandException.ConfigurationError(CommandAction.COMMANDKEY_IS_NULL);
      }
      if (LibStr.isEmptyOrNull(pMissingKey.get())) {
        CommandException.ConfigurationError(CommandAction.MISSINGKEY_IS_NULL);
      }
      if (pCommandTable.get() == null) {
        CommandException.ConfigurationError(CommandAction.COMMANDTABLE_IS_NULL);
      }
      commandMap = new CSVMap(pCommandTable.get().toString(), CommandAction.CSV_SEPARATOR, CommandAction.CSV_QUOTE, CommandAction.CSV_COMMENT, CommandAction.CSV_ENCODING);
    }
    else {
      commandMap = null;
      if (LibStr.isEmptyOrNull(pCommand.get())) {
        CommandException.ConfigurationError(CommandAction.COMMAND_IS_NULL);
      }
    }
  }

  public String getCommand(final StringSubstitutor substitutor) {
    String command = null;
    if (pIsCommandKey.get()) {
      String key = pCommandKey.get();
      if (substitutor != null) {
        key = substitutor.replace(key);
      }
      command = commandMap.getData(key);
      if (command == null) {
        command = commandMap.getData(pMissingKey.get());
      }
    }
    else {
      command = pCommand.get();
    }
    return command;
  }

  public long getOutputBufferSize() {
    return pOutputBufferSize.get();
  }

  public boolean hasCapture() {
    return pCapture.get();
  }

  protected String packOutAndErr(final ByteArrayOutputStream out, final ByteArrayOutputStream err) {
    final StringBuilder sb = new StringBuilder(out.size() + err.size() + 100);
    sb.append("*** Output stream ***").append(Helper.NL);
    try {
      if (out != null) {
        sb.append(out.toString(Helper.DEFAULT_ENCODING)).append(Helper.NL);
      }
    }
    catch (final UnsupportedEncodingException e) {
    }
    sb.append("*** End of Output stream ***").append(Helper.NL);
    sb.append("*** Error stream ***").append(Helper.NL);
    try {
      if (err != null) {
        sb.append(err.toString(Helper.DEFAULT_ENCODING)).append(Helper.NL);
      }
    }
    catch (final UnsupportedEncodingException e) {
    }
    sb.append("*** End of Error stream ***").append(Helper.NL);
    return sb.toString();
  }

}
