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
package net.eiroca.library.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

public class ResourceLocator implements Serializable {

  private static final long serialVersionUID = 2913302687024012546L;

  private final String name;
  private File file;
  private URL url;

  private static File searchDirectories(final String[] paths, final String filename) {
    SecurityException exception = null;
    for (final String element : paths) {
      try {
        final File file = new File(element, filename);
        if (file.exists() && !file.isDirectory()) { return file; }
      }
      catch (final SecurityException e) {
        // Security exceptions can usually be ignored, but if all attempts
        // to find the file fail, report the (last) security exception.
        exception = e;
      }
    }
    // Couldn't find any match
    if (exception != null) { throw exception; }
    return null;
  }

  private static File urlToFile(final URL res) {
    final String externalForm = res.toExternalForm();
    if (externalForm.startsWith("file:")) { return new File(externalForm.substring(5)); }
    return null;
  }

  public ResourceLocator(final String name) throws IOException {
    this.name = name;
    SecurityException exception = null;
    try {
      // Search using the CLASSPATH. If found, "file" is set and the call
      // returns true. A SecurityException might bubble up.
      if (tryClasspath(name)) { return; }
    }
    catch (final SecurityException e) {
      exception = e; // Save for later.
    }
    try {
      // Search using the classloader getResource( ). If found as a file,
      // "file" is set; if found as a URL, "url" is set.
      if (tryLoader(name)) { return; }
    }
    catch (final SecurityException e) {
      exception = e; // Save for later.
    }
    // If you get here, something went wrong. Report the exception.
    String msg = "";
    if (exception != null) {
      msg = ": " + exception;
    }
    throw new IOException("Resource '" + name + "' could not be found in the CLASSPATH (" + System.getProperty("java.class.path") + "), nor could it be located by the classloader responsible for the web application (WEB-INF/classes)" + msg);
  }

  /**
   * Method findResource.
   * @param fileName
   * @return InputStream
   */
  public InputStream findResource(final String fileName) {
    return getClass().getClassLoader().getResourceAsStream(fileName);
  }

  /**
   * Returns the directory containing the resource, or null if the resource isn't directly available
   * on the filesystem. This value can be used to locate the configuration file on disk, or to write
   * files in the same directory.
   */
  public String getDirectory() {
    if (file != null) {
      return file.getParent();
    }
    else if (url != null) { return null; }
    return null;
  }

  /**
   * Returns the file.
   * @return File
   */
  public File getFile() {
    return file;
  }

  /**
   * Returns an input stream to read the resource contents
   */
  public InputStream getInputStream() throws IOException {
    if (file != null) {
      return new BufferedInputStream(new FileInputStream(file));
    }
    else if (url != null) { return new BufferedInputStream(url.openStream()); }
    return null;
  }

  /**
   * Returns the resource name, as passed to the constructor
   */
  public String getName() {
    return name;
  }

  /**
   * Returns when the resource was last modified. If the resource was found using a URL, this method
   * will work only if the URL connection supports last modified information. If there's no support,
   * Long.MAX_VALUE is returned. Perhaps this should return -1, but you should return MAX_VALUE on
   * the assumption that if you can't determine the time, it's maximally new.
   */
  public long lastModified() {
    long result = Long.MAX_VALUE;
    if (file != null) {
      result = file.lastModified();
    }
    else if (url != null) {
      try {
        result = url.openConnection().getLastModified();
      }
      catch (final IOException e) {
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "[Resource: File: " + file + " URL: " + url + "]";
  }

  // Returns true if found
  private boolean tryClasspath(final String filename) {
    final String classpath = System.getProperty("java.class.path");
    final String[] paths = classpath.split(File.pathSeparator);
    file = ResourceLocator.searchDirectories(paths, filename);
    return (file != null);
  }

  // Returns true if found
  private boolean tryLoader(String name) {
    name = "/" + name;
    final URL res = ResourceLocator.class.getResource(name);
    if (res == null) { return false; }
    // Try converting from a URL to a File.
    final File resFile = ResourceLocator.urlToFile(res);
    if (resFile != null) {
      file = resFile;
    }
    else {
      url = res;
    }
    return true;
  }

}
