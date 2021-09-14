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
package net.eiroca.library.csv;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.eiroca.library.core.LibStr;

public class PairValidator {

  private char separator = '\t';

  private Set<String> parents = new HashSet<>();
  private Set<String> childs = new HashSet<>();
  private Set<String> pairs = new HashSet<>();

  public PairValidator() {
  }

  public PairValidator(final String path) {
    loadFromCSV(path, CSV.SEPARATOR, CSV.QUOTE, CSV.COMMENT, CSV.ENCODING);
  }

  public PairValidator(final String path, final char sep, final char quote, final char com, final String encoding) {
    loadFromCSV(path, sep, quote, com, encoding);
  }

  public void loadFromCSV(final String path, final char sep, final char quote, final char com, final String encoding) {
    separator = sep;
    final CSVData data = new CSVData(path, sep, quote, com, encoding);
    if ((data.getFieldNames().size() < 2)) {
      System.err.println("INVALID VALIDATOR DEFINITION (at least 2 colomns) " + path);
      return;
    }
    for (final String[] def : data.getData()) {
      addPair(def[0], def[1]);
    }
  }

  public void addPair(final String parent, final String child) {
    parents.add(parent);
    final StringBuffer pair = new StringBuffer();
    if (parent != null) {
      pair.append(parent);
    }
    if (LibStr.isNotEmptyOrNull(child)) {
      childs.add(child);
      pair.append(separator).append(child);
    }
    pairs.add(pair.toString());
  }

  public boolean isValidParent(final String parent) {
    return LibStr.isEmptyOrNull(parent) || parents.contains(parent);
  }

  public boolean isValidChild(final String child) {
    return LibStr.isEmptyOrNull(child) || childs.contains(child);
  }

  public boolean isValid(final String parent, final String child) {
    boolean valid = true;
    valid = valid && isValidParent(parent);
    valid = valid && isValidChild(child);
    if (LibStr.isNotEmptyOrNull(parent) && LibStr.isNotEmptyOrNull(child)) {
      final StringBuffer pair = new StringBuffer();
      pair.append(parent).append(separator).append(child);
      valid = valid && pairs.contains(pair.toString());
    }
    return valid;
  }

  public boolean validateMap(final String parentField, final String childField, final Map<String, String> data) {
    final String parent = data.get(parentField);
    final String child = data.get(childField);
    boolean valid = true;
    valid = valid && isValidParent(parent);
    valid = valid && isValidChild(child);
    valid = valid && isValid(parent, child);
    return valid;
  }

  public char getSeparator() {
    return separator;
  }

}
