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
package net.eiroca.ext.library.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import net.eiroca.library.core.Helper;

public class WritableEntity extends AbstractHttpEntity implements Cloneable {

  private static final String CONTENT_ENCODING = "deflate";
  protected ByteArrayOutputStream buffer = null;
  protected DeflaterOutputStream gzipper = null;
  boolean deflate;
  protected byte[] content = new byte[0];
  String charset;

  public WritableEntity(final String mimetype, final String charset, final boolean deflate) {
    this(ContentType.create(mimetype, charset).toString(), deflate);
    this.charset = charset;
  }

  public WritableEntity(final String contentType, final boolean deflate) {
    setContentType(contentType);
    if (deflate) {
      setContentEncoding(WritableEntity.CONTENT_ENCODING);
    }
    this.deflate = deflate;
  }

  public boolean setContent(final byte[] data) {
    boolean result = true;
    final OutputStream os = openBuffer();
    try {
      os.write(data);
    }
    catch (final IOException e) {
      result = false;
    }
    closeBuffer();
    return result;

  }

  public boolean setContent(final String text) {
    try {
      if (charset != null) {
        return setContent(text.getBytes(charset));
      }
      else {
        return setContent(text.getBytes());
      }
    }
    catch (final UnsupportedEncodingException e) {
      return false;
    }
  }

  public OutputStream openBuffer() {
    buffer = new ByteArrayOutputStream();
    gzipper = null;
    if (deflate) {
      final Deflater d = new Deflater();
      d.setLevel(Deflater.BEST_SPEED);
      gzipper = new DeflaterOutputStream(buffer, d);
      return gzipper;
    }
    return buffer;
  }

  public void closeBuffer() {
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
