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
package net.eiroca.library.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.eiroca.library.core.Helper;

final public class LibFile {

  private static final String ENCODING_UTF8 = "UTF-8";
  private static final String LINE_SEP = "\n";
  private static final int BUFFER_SIZE = 4096;

  public static Exception lastError;

  final public static void saveStrings(final String path, final Iterable<?> objects) {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(path));
      for (final Object line : objects) {
        bw.write((line != null ? line.toString() : ""));
        bw.write(LibFile.LINE_SEP);
      }
    }
    catch (final IOException e) {
      LibFile.lastError = e;
      System.err.println("IOException writing " + path);
    }
    finally {
      Helper.close(bw);
    }
  }

  final public static void readStrings(final String path, final List<String> data) {
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(path));
      while ((line = br.readLine()) != null) {
        data.add(line);
      }
    }
    catch (final FileNotFoundException e) {
      LibFile.lastError = e;
      System.err.println("File not found " + path);
    }
    catch (final IOException e) {
      LibFile.lastError = e;
      System.err.println("IOException reading " + path);
    }
    finally {
      Helper.close(br);
    }
  }

  final public static String readString(final String path) {
    return LibFile.readString(path, LibFile.ENCODING_UTF8);
  }

  public static String readString(final Reader reader) throws IOException {
    final StringBuffer data = new StringBuffer();
    BufferedReader br = null;
    try {
      br = new BufferedReader(reader);
      int ch;
      while ((ch = br.read()) != -1) {
        data.append((char)ch);
      }
    }
    finally {
      Helper.close(br, reader);
    }
    return data.toString();
  }

  public static String readString(final InputStream inputStream, final String encoding) {
    try {
      return LibFile.readString(new InputStreamReader(inputStream, encoding));
    }
    catch (final UnsupportedEncodingException e) {
      return null;
    }
    catch (final IOException e) {
      LibFile.lastError = e;
      System.err.println("IOException reading data");
      return null;
    }
  }

  final public static String readString(final String path, final String encoding) {
    FileInputStream is = null;
    Reader reader = null;
    try {
      is = new FileInputStream(path);
      reader = new InputStreamReader(is, encoding);
      return LibFile.readString(reader);
    }
    catch (final FileNotFoundException e) {
      LibFile.lastError = e;
      System.err.println("File not found " + path);
    }
    catch (final IOException e) {
      LibFile.lastError = e;
      System.err.println("IOException reading " + path);
    }
    finally {
      Helper.close(reader, is);
    }
    return null;
  }

  public static boolean appendString(final String path, final String data) {
    Writer fileWriter = null;
    boolean result = false;
    try {
      fileWriter = new FileWriter(path, true);
      fileWriter.write(data);
      fileWriter.close();
      result = true;
    }
    catch (final IOException e) {
      LibFile.lastError = e;
      System.err.println("IOException writing " + path);
    }
    finally {
      Helper.close(fileWriter);
    }
    return result;
  }

  public static boolean writeString(final String path, final String data) {
    Writer fileWriter = null;
    boolean result = false;
    try {
      fileWriter = new FileWriter(path, false);
      fileWriter.write(data);
      result = true;
    }
    catch (final IOException e) {
      LibFile.lastError = e;
      System.err.println("IOException writing " + path);
    }
    finally {
      Helper.close(fileWriter);
    }
    return result;
  }

  public static void writeBytes(final String file, final byte[] bytes) throws IOException {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(file);
      output.write(bytes);
    }
    finally {
      Helper.close(output);
    }
  }

  public static byte[] getBytesFromResource(final String resource) throws IOException {
    return LibFile.getBytes(LibFile.getResourceStream(resource, null));
  }

  public static byte[] getBytesFromFile(final String file) throws IOException {
    final InputStream source = new FileInputStream(file);
    return LibFile.getBytes(source);
  }

  public static byte[] getBytesFrom(final String fileOrResouce) throws IOException {
    byte[] bytes = null;
    final File file = new File(fileOrResouce);
    if (file.exists() && file.isFile()) {
      bytes = LibFile.getBytesFromFile(fileOrResouce);
    }
    else {
      bytes = LibFile.getBytesFromResource(fileOrResouce);
    }
    return bytes;
  }

  public static byte[] getBytes(final InputStream input) throws IOException {
    ByteArrayOutputStream output = null;
    try {
      output = new ByteArrayOutputStream();
      final byte buffer[] = new byte[LibFile.BUFFER_SIZE];
      int length;
      while ((length = input.read(buffer)) >= 0) {
        output.write(buffer, 0, length);
      }
      return output.toByteArray();
    }
    finally {
      Helper.close(output, input);
    }
  }

  public static InputStream getResourceStream(final String resourceName, final Class<?> callingClass) {
    InputStream is = null;
    final URL url = Helper.getResourceURL(resourceName, callingClass);
    if (url != null) {
      try {
        is = url.openStream();
      }
      catch (final IOException e) {
      }
    }
    return is;
  }

  public static String readFile(final String resourceName) {
    return LibFile.readFile(resourceName, null);
  }

  public static String readFile(final String resourceName, final Class<?> callingClass) {
    String result = null;
    final URL url = Helper.getResourceURL(resourceName, callingClass);
    if (url != null) {
      InputStream is = null;
      Scanner scanner = null;
      try {
        is = url.openStream();
        scanner = new Scanner(is);
        result = scanner.useDelimiter("\\Z").next();
      }
      catch (final IOException e) {
      }
      finally {
        Helper.close(scanner, is);
      }
    }
    return result;
  }

  public static String getFileKey(final Path path) {
    String key = path.toString();
    try {
      final BasicFileAttributeView view = Files.getFileAttributeView(path, BasicFileAttributeView.class);
      if (view != null) {
        final BasicFileAttributes attributes = view.readAttributes();
        final Object fk = attributes.fileKey();
        if (fk == null) {
          key = String.format("(p=%08X,ct=%08X)", key.hashCode(), attributes.creationTime().toString().hashCode());
        }
        else {
          key = fk.toString();
        }
      }
    }
    catch (final IOException e) {
      Logs.ignore(e);
      key = String.format("(p=%08X)", key.hashCode());
    }
    return key;
  }

  public static String getFileKey(final File file) {
    final Path path = file.toPath();
    final String key = LibFile.getFileKey(path);
    return key;
  }

  public static InputStream findResource(final String... paths) {
    InputStream inputStream = null;
    String foundPath = null;
    for (final String path : paths) {
      if (Files.exists(Paths.get(path))) {
        foundPath = path;
        break;
      }
    }
    if (foundPath != null) {
      try {
        inputStream = new FileInputStream(foundPath);
        return inputStream;
      }
      catch (final FileNotFoundException e) {
        // should never happen
      }
    }
    // look inside the classpath
    for (final String path : paths) {
      inputStream = LibFile.getResourceStream(path, null);
      if (inputStream != null) {
        break;
      }
    }
    return inputStream;
  }

  public static void getFiles(final String paths, final List<URI> filesList) {
    for (final String path : paths.split(File.pathSeparator)) {
      LibFile.getFiles(new File(path), filesList);
    }
  }

  public static void getFiles(final File file, final List<URI> filesList) {
    if (file.isDirectory()) {
      final File list[] = file.listFiles();
      for (final File f : list) {
        LibFile.getFiles(f, filesList);
      }
    }
    else {
      filesList.add(file.toURI());
    }
  }

  public static void getClassPathFiles(final List<URI> filesList) {
    LibFile.getFiles(System.getProperty("java.class.path"), filesList);
  }

  public static void getJarContent(final String jarPath, final List<String> content) {
    try (JarFile jarFile = new JarFile(jarPath);) {
      final Enumeration<JarEntry> e = jarFile.entries();
      while (e.hasMoreElements()) {
        final JarEntry entry = e.nextElement();
        final String name = entry.getName();
        content.add(name);
      }
    }
    catch (final IOException e1) {
    }
  }

  public static Properties loadConfiguration(final String... paths) {
    final Properties properties = new Properties();
    properties.putAll(System.getProperties());
    final InputStream prop = LibFile.findResource(paths);
    if (prop != null) {
      try {
        final Properties localConf = Helper.loadProperties(prop, false);
        properties.putAll(localConf);
      }
      catch (final IOException e) {
      }
    }
    return properties;
  }

}
