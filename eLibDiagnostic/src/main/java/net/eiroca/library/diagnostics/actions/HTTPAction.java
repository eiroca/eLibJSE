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

import net.eiroca.library.config.parameter.BooleanParameter;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;

public abstract class HTTPAction extends BaseAction {

  protected final BooleanParameter pHasProxy = new BooleanParameter(params, "useProxy", false, true, false);
  protected final StringParameter pProxyHost = new StringParameter(params, "proxyHost", null, false, true);
  protected final IntegerParameter pProxyPort = new IntegerParameter(params, "proxyPort", 8080, false, true);
  protected final StringParameter pProxyUser = new StringParameter(params, "proxyUsername", null, false, true);
  protected final StringParameter pProxyPass = new StringParameter(params, "proxyPassword", null, false, true);

}
