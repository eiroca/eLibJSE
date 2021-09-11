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
package net.eiroca.library.csv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibParser;
import net.eiroca.library.core.LibStr;

public class CSV {

  public static final String ENCODING = "UTF-8";
  public static final char COMMENT = '#';
  public static final char SEPARATOR = ';';
  public static final char QUOTE = '"';
  public static final String LF = "\n";

  public static void read(final ICSVReader reader, final String csvFile) {
    CSV.read(reader, csvFile, CSV.SEPARATOR, CSV.QUOTE, CSV.COMMENT, CSV.ENCODING);
  }

  public static void read(final ICSVReader reader, final String csvFile, final char sepChar, final char quoteChar, final char comment, final String encoding) {
    BufferedReader br = null;
    InputStreamReader is = null;
    String line = "";
    try {
      is = new InputStreamReader(new FileInputStream(csvFile), encoding);
      br = new BufferedReader(is);
      String headerLine;
      // Get header line from CSV file
      if ((headerLine = br.readLine()) != null) {
        final List<String> fieldNames = LibParser.split(headerLine, sepChar, quoteChar);
        reader.notifyHeaders(fieldNames);
        // Iterate over the lines of CSV file
        while ((line = br.readLine()) != null) {
          if (LibStr.isEmptyOrNull(line) || (line.charAt(0) == comment)) {
            continue;
          }
          final List<String> csvLine = LibParser.split(line, sepChar, quoteChar);
          if (csvLine.size() != fieldNames.size()) {
            reader.notifyError("Invalid row " + line);
          }
          else {
            reader.notifyRow(csvLine);
          }
        }
      }
      else {
        throw new IOException("CSV file is empty!");
      }
    }
    catch (final FileNotFoundException e) {
      reader.notifyError("File not found " + csvFile);
    }
    catch (final IOException e) {
      reader.notifyError("IOException reading " + csvFile);
    }
    finally {
      Helper.close(br, is);
    }
  }

}
