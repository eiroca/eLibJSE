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
package net.eiroca.ext.library.smb;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import net.eiroca.library.system.Logs;

public class LibSmb {

  transient private static final Logger logger = Logs.getLogger();

  public static final int SHARE_ALL = SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE | SmbFile.FILE_SHARE_DELETE;
  public static final int SHARE_NODELETE = SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE;

  private static CIFSContext smbContext;

  public static synchronized CIFSContext getCIFSContext() {
    if (LibSmb.smbContext == null) {
      LibSmb.smbContext = LibSmb.newCIFSContext(System.getProperties());
    }
    return LibSmb.smbContext;
  }

  private static final Map<String, CIFSContext> cache = new HashMap<>();

  public static synchronized CIFSContext getContext(final NtlmPasswordAuthenticator principal) {
    final String key = principal.getName() + "\t" + principal.getPassword();
    CIFSContext context = LibSmb.cache.get(key);
    if (context == null) {
      context = LibSmb.getCIFSContext();
      if (context != null) {
        context = context.withCredentials(principal);
        LibSmb.cache.put(key, context);
      }
    }
    return context;
  }

  public static CIFSContext newCIFSContext(final Properties p) {
    PropertyConfiguration config;
    try {
      config = new PropertyConfiguration(p);
      return new BaseContext(config);
    }
    catch (final CIFSException e) {
      LibSmb.logger.error("Fatal error CIFS context creation: " + e.getMessage(), e);
      return null;
    }
  }

  public static SmbFile build(final String url, final String domain, final String username, final String password) {
    return LibSmb.build(url, LibSmb.getContext(new NtlmPasswordAuthenticator(domain, username, password)));
  }

  public static SmbFile build(final String url, final NtlmPasswordAuthenticator principal) {
    return LibSmb.build(url, LibSmb.getContext(principal));
  }

  public static SmbFile build(final String url, final CIFSContext context) {
    try {
      return new SmbFile(url, context);
    }
    catch (final MalformedURLException e) {
      LibSmb.logger.error("Invalid SMB file {}", url);
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
