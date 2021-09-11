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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.Logs;

public class CSVData implements ICSVReader {

  private static final Logger logger = Logs.getLogger();

  private final List<String[]> data = new ArrayList<>();
  private List<String> fieldNames;

  public CSVData(final String csvFile) {
    readCSV(csvFile, CSV.SEPARATOR, CSV.QUOTE, CSV.COMMENT, CSV.ENCODING);
  }

  public CSVData(final String csvFile, final char sepChar, final char quoteChar, final char comment, final String encoding) {
    readCSV(csvFile, sepChar, quoteChar, comment, encoding);
  }

  public void readCSV(final String csvFile, final char sepChar, final char quoteChar, final char comment, final String encoding) {
    data.clear();
    CSV.read(this, csvFile, sepChar, quoteChar, comment, encoding);
  }

  public String getFieldName(final int i) {
    String result = null;
    if ((fieldNames != null) && (i >= 0) && (i < fieldNames.size())) {
      result = fieldNames.get(i);
    }
    return result;
  }

  public List<String> getFieldNames() {
    return fieldNames;
  }

  public List<String[]> getData() {
    return data;
  }

  public int size() {
    return data.size();
  }

  public String[] getData(final int index) {
    return data.get(index);
  }

  @Override
  public void notifyHeaders(final List<String> headers) {
    fieldNames = headers;
  }

  @Override
  public void notifyRow(final List<String> row) {
    data.add(LibStr.toArray(row));
  }

  @Override
  public void notifyError(final String message) {
    CSVData.logger.error(message);
  }

}
