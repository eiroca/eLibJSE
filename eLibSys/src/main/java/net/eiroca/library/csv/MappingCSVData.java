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

import java.util.HashMap;
import java.util.Map;

public class MappingCSVData implements ICSVReader {

  private final Map<String, Map<String, String>> valuesMap = new HashMap<>();

  private String[] fieldNames;

  public String name;

  public MappingCSVData() {
  }

  public MappingCSVData(final String csvFile) {
    readCSV(csvFile, CSV.SEPARATOR, CSV.COMMENT, CSV.ENCODING);
  }

  public MappingCSVData(final String csvFile, final String csvSeparatorChar, final String comment, final String encoding) {
    readCSV(csvFile, csvSeparatorChar, comment, encoding);
  }

  public void readCSV(final String csvFile, final String csvSeparatorChar, final String comment, final String encoding) {
    name = csvFile;
    valuesMap.clear();
    CSV.read(this, csvFile, csvSeparatorChar, comment, encoding);
  }

  public String getFieldName(final int i) {
    String result = null;
    if ((fieldNames != null) && (i >= 0) && (i < fieldNames.length)) {
      result = fieldNames[i];
    }
    return result;
  }

  public Map<String, String> get(final String key) {
    return valuesMap.get(key);
  }

  public Map<String, Map<String, String>> getValuesMap() {
    return valuesMap;
  }

  @Override
  public void notifyHeaders(final String[] headers) {
    fieldNames = headers;
  }

  @Override
  public void notifyRow(final String[] row) {
    final String matchValue = row[0];
    final Map<String, String> auxMap = new HashMap<>();
    for (int i = 1; i < fieldNames.length; i++) {
      auxMap.put(fieldNames[i], row[i]);
    }
    valuesMap.put(matchValue, auxMap);
  }

  @Override
  public void notifyError(final String message) {
    System.err.println(message);
  }

}
