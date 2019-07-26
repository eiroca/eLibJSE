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
package net.eiroca.library.system;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Pipe implements Runnable {

  private static final int READ_BUFFER = 4096;

  private final InputStream in;
  private final OutputStream out;
  private final long maxSize;
  private Exception lastError = null;
  private boolean copying = true;

  private Pipe(final InputStream in, final OutputStream out, final long maxSize) {
    this.in = in;
    this.out = out;
    this.maxSize = maxSize;
    lastError = null;
  }

  public static void pipe(final Process process, final ByteArrayOutputStream out, final ByteArrayOutputStream err, final long maxSize) {
    Pipe.pipe(process.getInputStream(), out, maxSize);
    Pipe.pipe(process.getErrorStream(), err, maxSize);
  }

  public static void pipe(final InputStream in, final OutputStream out, final long size) {
    final Thread thread = new Thread(new Pipe(in, out, size));
    thread.setDaemon(true);
    thread.start();
  }

  public static void copy(final InputStream in, final OutputStream out, final long maxSize) throws IOException {
    int byteRead;
    final byte[] buf = new byte[Pipe.READ_BUFFER];
    int bufSize = 0;
    boolean copying = true;
    while (copying && ((byteRead = in.read(buf)) != -1)) {
      int copySize = byteRead;
      if ((bufSize + copySize) > maxSize) {
        copySize = (int)(maxSize - bufSize);
        copying = false;
      }
      if (copySize > 0) {
        out.write(buf, 0, copySize);
        bufSize = bufSize + copySize;
      }
    }
  }

  @Override
  public void run() {
    try {
      int byteRead;
      final byte[] buf = new byte[Pipe.READ_BUFFER];
      int bufSize = 0;
      copying = true;
      while (copying && ((byteRead = in.read(buf)) != -1)) {
        int copySize = byteRead;
        if ((bufSize + copySize) > maxSize) {
          copySize = (int)(maxSize - bufSize);
          copying = false;
        }
        if (copySize > 0) {
          out.write(buf, 0, copySize);
          bufSize = bufSize + copySize;
        }
      }
    }
    catch (final Exception e) {
      lastError = e;
      copying = false;
    }
    return;
  }

  public Exception getLastError() {
    return lastError;
  }

  public void setLastError(final Exception lastError) {
    this.lastError = lastError;
  }

  public boolean isCopying() {
    return copying;
  }

  public void setCopying(final boolean copying) {
    this.copying = copying;
  }

}
