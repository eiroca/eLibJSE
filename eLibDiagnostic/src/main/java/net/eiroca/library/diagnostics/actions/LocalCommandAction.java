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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.StringSubstitutor;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.actiondata.ActionData;
import net.eiroca.library.diagnostics.actiondata.Messages;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.system.Pipe;

public class LocalCommandAction extends CommandAction {

  private static final String REGEX_COMMANDTOKEN = "[^\\s\"']+|\"[^\"]*\"|'[^']*'";
  private static final Pattern tokenizer = Pattern.compile(LocalCommandAction.REGEX_COMMANDTOKEN);

  @Override
  public ReturnObject execute(ActionData action) throws CommandException {
    ReturnObject result;
    if (action == null) {
      action = new ActionData();
    }
    action.set(ActionData.PARAM, getParameter());
    final Map<String, String> data = action.getData(converter);
    final StringSubstitutor substitutor = new StringSubstitutor(data);
    final String command = getCommand(substitutor);
    if (LibStr.isEmptyOrNull(command)) {
      CommandException.ConfigurationError(CommandAction.COMMAND_IS_INVALID);
    }
    data.put(ActionData.COMAMND, converter.convert(command));
    data.put(Messages.RAWPREFIX + ActionData.COMAMND, command);
    context.info(action.toString());
    final String[] tokens = prepareCommand(command);
    final String[] as = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      as[i] = substitutor.replace(tokens[i]);
    }
    context.info("Execute command: '" + LibStr.merge(as, " ", "") + "'");
    try {
      result = executeCommand(as, hasCapture(), getOutputBufferSize());
    }
    catch (IOException | InterruptedException e) {
      context.warn("Execute command: " + Helper.getExceptionAsString(e));
      result = null;
    }
    final String output = (result != null) ? result.getOutput() : null;
    context.info("Execute command: RC = " + ((result != null) ? result.getRetCode() : "?"));
    context.debug("Execute command: output = '" + output + "'");
    processMetrics(result, Helper.NL, Helper.LISTSEPERATOR);
    return result;
  }

  private String[] prepareCommand(final String subjectString) {
    final ArrayList<String> matchList = new ArrayList<>();
    final Matcher regexMatcher = LocalCommandAction.tokenizer.matcher(subjectString);
    while (regexMatcher.find()) {
      matchList.add(regexMatcher.group());
    }
    return matchList.toArray(new String[matchList.size()]);
  }

  private ReturnObject executeCommand(final String[] command, final boolean capture, final long size) throws IOException, InterruptedException {
    // add threads to read out and err
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();
    final Process child = Runtime.getRuntime().exec(command);
    if (capture) {
      Pipe.pipe(child, out, err, size);
    }
    child.waitFor();
    final Integer rc = child.exitValue();
    final ReturnObject obj = new ReturnObject();
    obj.setOutput(packOutAndErr(out, err));
    obj.setRetCode(rc);
    if (child != null) {
      child.destroy();
    }
    return obj;
  }
}
