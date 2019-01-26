/**
 *
 * Copyright (C) 2001-2019 eIrOcA (eNrIcO Croce & sImOnA Burzio) - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.ext.library.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import net.eiroca.library.core.Helper;

public class WritableEntity extends AbstractHttpEntity implements Cloneable {

  protected ByteArrayOutputStream buffer = null;
  protected DeflaterOutputStream gzipper = null;
  boolean deflate;
  protected byte[] content = new byte[0];

  public WritableEntity(final String contentType, final String mimetype, final String charset, final boolean deflate) {
    setContentType(ContentType.create(mimetype, charset).toString());
    if (deflate) {
      setContentEncoding("gzip");
    }
    this.deflate = deflate;
  }

  public OutputStream openBuffer() {
    buffer = new ByteArrayOutputStream();
    gzipper = null;
    if (deflate) {
      gzipper = new DeflaterOutputStream(buffer);
      return gzipper;
    }
    return buffer;
  }

  public void closeByuffer() {
    Helper.close(gzipper, buffer);
    content = buffer.toByteArray();
  }

  @Override
  public boolean isRepeatable() {
    return true;
  }

  @Override
  public long getContentLength() {
    return content.length;
  }

  @Override
  public InputStream getContent() throws IOException, IllegalStateException {
    return new ByteArrayInputStream(content);
  }

  @Override
  public void writeTo(final OutputStream outstream) throws IOException {
    if (outstream == null) { throw new IllegalArgumentException("Output stream may not be null"); }
    outstream.write(content);
    outstream.flush();
  }

  @Override
  public boolean isStreaming() {
    return false;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

}
