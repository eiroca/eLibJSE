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
package net.eiroca.library.csv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import net.eiroca.library.core.Helper;
import net.eiroca.library.system.Logs;

public class CSVMap implements ICSVReader {

  private static final Logger logger = Logs.getLogger();

  private final Map<String, String> data = new HashMap<>();
  private List<String> fieldNames;

  public CSVMap() {
    fieldNames = new ArrayList<>();
    fieldNames.add("key");
    fieldNames.add("value");
  }

  public CSVMap(final String csvFile, final char sepChar, final char quoteChar, final char comment, final String encoding) {
    readCSV(csvFile, sepChar, quoteChar, comment, encoding);
  }

  public void readCSV(final String csvFile, final char sepChar, final char quoteChar, final char comment, final String encoding) {
    data.clear();
    CSV.read(this, csvFile, sepChar, quoteChar, comment, encoding);
  }

  public void saveCSV(final String csvFile, final char csvSeparatorChar) {
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(csvFile));
      for (int i = 0; i < fieldNames.size(); i++) {
        if (i > 0) {
          bw.write(csvSeparatorChar);
        }
        bw.write(fieldNames.get(i));
      }
      bw.write(CSV.LF);
      for (final Map.Entry<String, String> e : data.entrySet()) {
        bw.write(e.getKey() + csvSeparatorChar + e.getValue() + CSV.LF);
      }
    }
    catch (final IOException e) {
      System.err.println("IOException writing " + csvFile);
    }
    finally {
      Helper.close(bw);
    }
  }

  public String getFieldName(final int i) {
    String result = null;
    if ((fieldNames != null) && (i >= 0) && (i < fieldNames.size())) {
      result = fieldNames.get(i);
    }
    return result;
  }

  public String getData(final String key) {
    return data.get(key);
  }

  public boolean hasData(final String key) {
    return data.containsKey(key);
  }

  public String getData(final String key, final String def) {
    String result = data.get(key);
    if (result == null) {
      result = def;
    }
    return result;
  }

  public String getOrSet(final String key, final String value) {
    String result = data.get(key);
    if (result == null) {
      result = value;
      data.put(key, value);
    }
    return result;
  }

  public void setData(final String key, final String value) {
    data.put(key, value);
  }

  public int size() {
    return data.size();
  }

  @Override
  public void notifyHeaders(final List<String> headers) {
    fieldNames = headers;
  }

  @Override
  public void notifyRow(final List<String> row) {
    data.put(row.get(0), row.get(1));
  }

  @Override
  public void notifyError(final String message) {
    CSVMap.logger.error(message);
  }

}
