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
package net.eiroca.library.csv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import net.eiroca.library.system.Logs;

public class MappingCSVData implements ICSVReader {

  private static final Logger logger = Logs.getLogger();

  private final Map<String, Map<String, String>> valuesMap = new HashMap<>();
  private List<String> fieldNames;

  public String name;

  public MappingCSVData() {
  }

  public MappingCSVData(final String csvFile) {
    readCSV(csvFile, CSV.SEPARATOR, CSV.QUOTE, CSV.COMMENT, CSV.ENCODING);
  }

  public MappingCSVData(final String csvFile, final char csvSeparatorChar, final char quoteChar, final char comment, final String encoding) {
    readCSV(csvFile, csvSeparatorChar, quoteChar, comment, encoding);
  }

  public void readCSV(final String csvFile, final char csvSeparatorChar, final char quoteChar, final char comment, final String encoding) {
    name = csvFile;
    valuesMap.clear();
    CSV.read(this, csvFile, csvSeparatorChar, quoteChar, comment, encoding);
  }

  public String getFieldName(final int i) {
    String result = null;
    if ((fieldNames != null) && (i >= 0) && (i < fieldNames.size())) {
      result = fieldNames.get(i);
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
  public void notifyHeaders(final List<String> headers) {
    fieldNames = headers;
  }

  @Override
  public void notifyRow(final List<String> row) {
    final String matchValue = row.get(0);
    final Map<String, String> auxMap = new HashMap<>();
    for (int i = 1; i < fieldNames.size(); i++) {
      auxMap.put(fieldNames.get(i), row.get(i));
    }
    valuesMap.put(matchValue, auxMap);
  }

  @Override
  public void notifyError(final String message) {
    MappingCSVData.logger.error(message);
  }

}
