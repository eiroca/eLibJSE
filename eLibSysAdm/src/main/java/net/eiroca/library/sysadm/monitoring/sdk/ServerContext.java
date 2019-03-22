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
package net.eiroca.library.sysadm.monitoring.sdk;

import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import net.eiroca.library.system.Context;

public class ServerContext extends Context {

  public ServerContext(String name, Properties properties) {
    super(name, properties);
  }

  @Override
  public String getConfigPassword(final String propName) {
     String result = config.get(propName);
    if (result != null) {
      Base64 base64 = new Base64();
      result = new String(base64.decode(result.getBytes()));
    }
    return result;
  }

}
