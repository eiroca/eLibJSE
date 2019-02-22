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
package net.eiroca.ext.library.smb;

import java.net.MalformedURLException;
import org.slf4j.Logger;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import net.eiroca.library.system.Logs;

public class LibSmb {

  transient private static final Logger logger = Logs.getLogger();

  public static final int SHARE_ALL = SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE | SmbFile.FILE_SHARE_DELETE;
  public static final int SHARE_NODELETE = SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE;

  public static SmbFile build(final String path, final String domain, final String username, final String password, final int shareMode) {
    final NtlmPasswordAuthentication principal = new NtlmPasswordAuthentication(domain, username, password);
    return LibSmb.build(path, principal, shareMode);
  }

  public static SmbFile build(final String path, final NtlmPasswordAuthentication principal, final int shareMode) {
    try {
      return new SmbFile(path, principal, shareMode);
    }
    catch (final MalformedURLException e) {
      LibSmb.logger.error("Invalid SMB file {}", path);
      return null;
    }
  }

  public static String getID(final SmbFile file) {
    final StringBuffer id = new StringBuffer();
    id.append('(');
    id.append(String.format("p=%08X", file.getURL().toString().hashCode()));
    try {
      id.append(String.format(",ct=%08X", Long.toString(file.createTime()).hashCode()));
    }
    catch (final SmbException e) {
    }
    id.append(')');
    return id.toString();
  }

  public static void walkFileTree(final SmbFile basePath, final int maxLevel, final ISMBFileVisitor visitor) throws SmbException {
    if (basePath.exists()) {
      LibSmb.logger.trace("Walking {}", basePath.getCanonicalPath());
      final SmbFile[] files = basePath.listFiles();
      for (final SmbFile f : files) {
        if (f.isFile()) {
          if (!visitor.visitFile(f)) {
            break;
          }
        }
        else if (f.isDirectory()) {
          if (maxLevel > 0) {
            LibSmb.walkFileTree(f, maxLevel - 1, visitor);
          }
        }
      }
    }
  }

}
