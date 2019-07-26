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
package net.eiroca.library.sysadm.monitoring.sdk;

import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import net.eiroca.library.system.Context;

public class ServerContext extends Context {

  private ICredentialProvider provider = null;

  public ServerContext(final String name, final Properties properties) {
    super(name, properties);
  }

  public void setCredentialProvider(final ICredentialProvider provider) {
    this.provider = provider;
  }

  @Override
  public String getConfigPassword(final String propName) {
    String pwd = null;
    final String result = config.get(propName);
    if (result != null) {
      if (provider != null) {
        pwd = provider.getPlainPassword(result);
      }
      if (pwd == null) {
        final Base64 base64 = new Base64();
        pwd = new String(base64.decode(result.getBytes()));
      }
    }
    return pwd;
  }

}
