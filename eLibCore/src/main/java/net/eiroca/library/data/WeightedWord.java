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
package net.eiroca.library.data;

public class WeightedWord {

  double weight;
  String word;

  public double getWeight() {
    return weight;
  }

  public String getWord() {
    return word;
  }

  public WeightedWord(final String word, final double weight) {
    super();
    this.weight = weight;
    this.word = word;
  }

  @Override
  public String toString() {
    return "WeightedWord [weight=" + weight + ", word='" + word + "']";
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) { return true; }
    if ((obj == null) || (getClass() != obj.getClass())) { return false; }
    final WeightedWord other = (WeightedWord)obj;
    if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight)) { return false; }
    if (word == null) {
      if (other.word != null) { return false; }
    }
    else if (!word.equals(other.word)) { return false; }
    return true;
  }

}
