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

import java.util.ArrayList;
import java.util.List;

public class CSVData implements ICSVReader {

  private final List<String[]> data = new ArrayList<>();
  private String[] fieldNames;

  public CSVData(final String csvFile) {
    readCSV(csvFile, CSV.SEPARATOR, CSV.COMMENT, CSV.ENCODING);
  }

  public CSVData(final String csvFile, final String csvSeparatorChar, final String comment, final String encoding) {
    readCSV(csvFile, csvSeparatorChar, comment, encoding);
  }

  public void readCSV(final String csvFile, final String csvSeparatorChar, final String comment, final String encoding) {
    data.clear();
    CSV.read(this, csvFile, csvSeparatorChar, comment, encoding);
  }

  public String getFieldName(final int i) {
    String result = null;
    if ((fieldNames != null) && (i >= 0) && (i < fieldNames.length)) {
      result = fieldNames[i];
    }
    return result;
  }

  public String[] getFieldNames() {
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
  public void notifyHeaders(final String[] headers) {
    fieldNames = headers;
  }

  @Override
  public void notifyRow(final String[] row) {
    data.add(row);
  }

  @Override
  public void notifyError(final String message) {
    System.err.println(message);
  }

}
