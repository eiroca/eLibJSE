/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.PasswordParameter;
import net.eiroca.library.config.parameter.StringParameter;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.diagnostics.CommandException;
import net.eiroca.library.diagnostics.actiondata.ActionData;
import net.eiroca.library.diagnostics.actiondata.Messages;
import net.eiroca.library.diagnostics.util.ReturnObject;
import net.eiroca.library.system.IContext;
import net.eiroca.library.system.Pipe;

public class SSHCommandAction extends CommandAction {

  private static final String USER_IS_NULL_OR_EMPTY = "The 'username' parameter is null or empty.";
  private static final String PASSWORD_IS_NULL = "The 'password' parameter is null.";
  private static final String KEY_FILE_IS_NULL_OR_EMPTY = "The 'keyFile' parameter is null or empty.";
  private static final String PASSPHRASE_IS_NULL = "The 'passphrase' parameter is null.";

  // Config
  final StringParameter pAuthMethod = new StringParameter(params, "authMethod", "Password", false, false);
  final StringParameter pHost = new StringParameter(params, "host", null, false, true);
  final IntegerParameter pPort = new IntegerParameter(params, "port", 22, false, false);
  final StringParameter pUsername = new StringParameter(params, "username", null, false, true);
  final PasswordParameter pPassword = new PasswordParameter(params, "password", null, false, true);
  final PasswordParameter pPassphrase = new PasswordParameter(params, "passphrase", null, false, true);
  final StringParameter pKeyFile = new StringParameter(params, "keyFile", "PublicKey", false, true);

  private Connection conn;
  private Session session;

  @Override
  public void setup(final IContext context) throws CommandException {
    context.debug("Setup");
    super.setup(context);
    if ("Password".equals(pAuthMethod.get())) {
      if (LibStr.isEmptyOrNull(pUsername.get())) {
        CommandException.ConfigurationError(SSHCommandAction.USER_IS_NULL_OR_EMPTY);
      }
      if (pPassword.get() == null) {
        CommandException.ConfigurationError(SSHCommandAction.PASSWORD_IS_NULL);
      }
      try {
        setup(pHost.get(), pPort.get(), pUsername.get(), pPassword.get());
      }
      catch (final Exception e) {
        CommandException.InfrastructureError(Helper.getExceptionAsString(e));
      }
    }
    else {
      if (LibStr.isEmptyOrNull(pKeyFile.get())) {
        CommandException.ConfigurationError(SSHCommandAction.KEY_FILE_IS_NULL_OR_EMPTY);
      }
      if (pPassphrase.get() == null) {
        CommandException.ConfigurationError(SSHCommandAction.PASSPHRASE_IS_NULL);
      }
      try {
        setup(pHost.get(), pPort.get(), pUsername.get(), pPassphrase.get(), pKeyFile.get());
      }
      catch (final Exception e) {
        CommandException.InfrastructureError(Helper.getExceptionAsString(e));
      }
    }
  }

  @Override
  public ReturnObject execute(ActionData action) throws CommandException {
    ReturnObject result;
    if (action == null) {
      action = new ActionData();
    }
    action.set(ActionData.PARAM, getParameter());
    action.set(ActionData.USER, pUsername.get());
    action.set(ActionData.PORT, String.valueOf(pPort.get()));
    action.set(ActionData.HOST, pHost.get());
    final Map<String, String> data = action.getData(converter);
    final StringSubstitutor substitutor = new StringSubstitutor(data);
    String command = getCommand(substitutor);
    if (LibStr.isEmptyOrNull(command)) {
      CommandException.ConfigurationError(CommandAction.COMMAND_IS_INVALID);
    }
    data.put(ActionData.COMAMND, converter.convert(command));
    data.put(Messages.RAWPREFIX + ActionData.COMAMND, command);
    context.info(action.toString());
    command = substitutor.replace(command);
    try {
      context.info("Execute command: '" + command + "'");
      result = executeCommand(command, "", getOutputBufferSize());
    }
    catch (final Exception e) {
      context.warn("Execute command: " + Helper.getExceptionAsString(e));
      result = null;
    }
    final String output = (result != null) ? result.getOutput() : null;
    context.info("Execute command: RC = " + ((result != null) ? result.getRetCode() : "?"));
    context.debug("Execute command: output = '" + output + "'");
    processMetrics(result, Helper.NL, Helper.LISTSEPERATOR);
    return result;
  }

  public ReturnObject executeCommand(final String cmd, String env, final long size) throws Exception {
    final ReturnObject obj = new ReturnObject();
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();
    InputStream stdOut = null;
    InputStream stdErr = null;
    session = conn.openSession();
    if (hasCapture()) {
      stdOut = new StreamGobbler(session.getStdout());
      stdErr = new StreamGobbler(session.getStderr());
    }
    session.requestPTY("vt100");
    if (env == null) {
      env = "";
    }
    final String finalCmd = new StringBuilder(env).append(LibStr.isNotEmptyOrNull(env) ? " " : "").append(cmd).toString();
    context.info("Execute command: '" + finalCmd + "'");
    session.execCommand(finalCmd);
    if (hasCapture()) {
      Pipe.copy(stdOut, out, size);
      Pipe.copy(stdErr, err, size);
    }
    session.close();
    obj.setOutput(packOutAndErr(out, err));
    obj.setRetCode(session.getExitStatus());
    return obj;
  }

  public void setup(final String host, final int port, final String user, final String pass) throws Exception {
    context.debug("setup host=", host, " port=", "" + port, "user=", user, "pwd=", LibStr.isNotEmptyOrNull(pass) ? "YES" : "NO");
    try {
      if (conn != null) {
        conn.close();
      }
      conn = new Connection(host, port);
      conn.connect();
      final boolean isAuthenticated = conn.authenticateWithPassword(user, pass);
      if (!isAuthenticated) { throw new IOException("Authentication failed."); }
    }
    catch (final IOException ex) {
      if (conn != null) {
        conn.close();
        conn = null;
      }
      throw ex;
    }
  }

  public void setup(final String host, final int port, final String user, final String pass, final String keyFile) throws Exception {
    try {
      if (conn != null) {
        conn.close();
      }
      conn = new Connection(host, port);
      conn.connect();
      context.info("SSH Publickey authentication");
      final boolean available = conn.isAuthMethodAvailable(user, "publickey");
      if (!available) { throw new IOException("Authentication-Method not Available"); }
      final File pemFile = new File(keyFile);
      final boolean isAuthenticated = conn.authenticateWithPublicKey(user, pemFile, pass);
      if (!isAuthenticated) { throw new IOException("Authentication failed."); }
    }
    catch (final IOException ex) {
      if (conn != null) {
        conn.close();
        conn = null;
      }
      throw ex;
    }
  }

  @Override
  public void close() throws Exception {
    super.close();
    if (conn != null) {
      conn.close();
      conn = null;
    }
  }

}
