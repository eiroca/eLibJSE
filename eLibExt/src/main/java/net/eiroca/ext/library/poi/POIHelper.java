/**
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
 **/
package net.eiroca.ext.library.poi;

import java.util.Date;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import net.eiroca.library.core.LibStr;

public class POIHelper {

  public static Long getLong(final Row row, final int col) {
    final Cell cell = row.getCell(col, MissingCellPolicy.CREATE_NULL_AS_BLANK);
    if (cell.getCellType() == CellType.NUMERIC) {
      return new Long((long)cell.getNumericCellValue());
    }
    else {
      return null;
    }
  }

  public static Date getDate(final Row row, final int col) {
    final Cell cell = row.getCell(col, MissingCellPolicy.CREATE_NULL_AS_BLANK);
    if (cell.getCellType() == CellType.NUMERIC) {
      return cell.getDateCellValue();
    }
    else {
      return null;
    }
  }

  public static void setDate(final Row row, final int col, final Date value, final CellStyle cellStyle) {
    if (value != null) {
      final Cell cell = row.createCell(col, CellType.NUMERIC);
      cell.setCellValue(value);
      if (cellStyle != null) {
        cell.setCellStyle(cellStyle);
      }
    }
  }

  public static String getString(final Row row, final int col) {
    return POIHelper.getString(row, col, true);
  }

  public static String getString(final Row row, final int col, final boolean trim) {
    final Cell cell = row.getCell(col, MissingCellPolicy.CREATE_NULL_AS_BLANK);
    String result = cell.toString();
    if (trim) {
      if (LibStr.isNotEmptyOrNull(result)) {
        result = result.replace((char)160, ' ').trim();
      }
    }
    return result;
  }

  public static void setString(final Row row, final int col, final String value) {
    if (LibStr.isNotEmptyOrNull(value)) {
      final Cell cell = row.createCell(col, CellType.STRING);
      try {
        cell.setCellValue(value);
      }
      catch (final Exception e) {
        System.err.println("INVALID STRING: " + value);
        throw e;
      }
    }
  }

  public static void setNumeric(final Row row, final int col, final Long value) {
    final Cell cell = row.createCell(col, CellType.NUMERIC);
    cell.setCellValue(value != null ? value : 0.0);
  }

  public static void setValue(final Row row, final int col, final double value) {
    final Cell cell = row.createCell(col, CellType.NUMERIC);
    cell.setCellValue(value);
  }

  public static String getStringValue(final Row row, final int col, final boolean trim) {
    final Cell cell = row.getCell(col, MissingCellPolicy.CREATE_NULL_AS_BLANK);
    String result;
    try {
      if (cell.getCellType() == CellType.FORMULA) {
        CellType type = cell.getCachedFormulaResultType();
        switch (type) {
          case NUMERIC:
            result = String.valueOf(cell.getNumericCellValue());
            break;
          case BOOLEAN:
            result = String.valueOf(cell.getBooleanCellValue());
            break;
          default:
            result = cell.getRichStringCellValue().toString();
            break;
        }
      }
      else {
        result = cell.getStringCellValue();
      }
    }
    catch (final java.lang.IllegalStateException e) {
      result = cell.toString();
    }
    if (trim) {
      if (LibStr.isNotEmptyOrNull(result)) {
        result = result.replace((char)160, ' ').trim();
      }
      if (result.endsWith(".0")) {
        result = result.substring(0, result.length() - 2);
      }
    }
    return result;
  }

  public static XSSFRow getRow(final XSSFSheet ws, final int rowNum) {
    XSSFRow result = ws.getRow(rowNum);
    if (result == null) {
      result = ws.createRow(rowNum);
    }
    return result;
  }

}
