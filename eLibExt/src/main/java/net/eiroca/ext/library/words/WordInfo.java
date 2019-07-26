/**
 *
 * Copyright (C) 1999-2019 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.ext.library.words;

public class WordInfo {

  enum WordType {
    WHITELIST, BLACKLIST, ITALIAN, ENGLISH, CHANGED, CORRECTED, UNKNOWN
  }

  public String word;
  public String newWord;
  public String suggested;
  public WordType type;
  public int count;

  public WordInfo(final String word) {
    count = 0;
    this.word = word;
    newWord = word;
    type = WordType.UNKNOWN;
  }

  @Override
  public String toString() {
    return word + "\t" + newWord + "\t" + (suggested != null ? suggested : "") + "\t" + type + "\t" + count;
  }

}
