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
package net.eiroca.library.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;

public class MultipleWords implements Comparator<WeightedWord> {

  private static final String FMT_PERCENT = "[%3.0f%%]";
  private static final String FMT_ABSOLUTE = "[%f]";

  public static boolean absoluteWeight = false;

  private final List<WeightedWord> words = new ArrayList<>();

  public MultipleWords() {

  }

  public synchronized void remove(final String word) {
    if (word == null) { return; }
    for (int i = 0; i < words.size(); i++) {
      final WeightedWord ww = words.get(i);
      if (ww.word.equals(word)) {
        words.remove(i);
        return;
      }
    }
  }

  public synchronized void add(final String word, final double weight) {
    if (word == null) { return; }
    WeightedWord ww;
    for (int i = 0; i < words.size(); i++) {
      ww = words.get(i);
      if (ww.word.equals(word)) {
        ww.weight += weight;
        if (ww.weight < 0.0) {
          words.remove(i);
        }
        return;
      }
    }
    if (weight >= 0) {
      words.add(new WeightedWord(word, weight));
    }
  }

  public synchronized void clear() {
    words.clear();
  }

  private synchronized void sort() {
    if (words.size() > 1) {
      Collections.sort(words, this);
    }
  }

  public synchronized String getBest() {
    sort();
    return words.size() > 0 ? words.get(0).word : null;
  }

  public synchronized void getBests(final String[] res, final int count) {
    sort();
    for (int i = 0; i < count; i++) {
      res[i] = (words.size() > i) ? words.get(i).word : null;
    }
  }

  public String toString(final String sep, final boolean addWeight) {
    return toString(sep, addWeight, 0);
  }

  public String toString(final String sep, final boolean addWeight, final double minWeight) {
    final StringBuilder sb = new StringBuilder();
    if (words.size() == 1) {
      sb.append(words.get(0).word);
    }
    else if (words.size() > 1) {
      sort();
      double totWeight = 0;
      if (addWeight && !MultipleWords.absoluteWeight) {
        for (final WeightedWord ww : words) {
          totWeight += ww.weight;
        }
        if (totWeight < 0.001) {
          totWeight = .01;
        }
      }
      for (int i = 0; i < words.size(); i++) {
        final WeightedWord ww = words.get(i);
        if (i > 0) {
          if (ww.weight < minWeight) {
            continue;
          }
          sb.append(sep);
        }
        sb.append(ww.word);
        if (addWeight) {
          if (MultipleWords.absoluteWeight) {
            sb.append(String.format(MultipleWords.FMT_ABSOLUTE, ww.weight));
          }
          else {
            sb.append(String.format(MultipleWords.FMT_PERCENT, (100 * ww.weight) / totWeight));

          }
        }
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return toString(",", true);
  }

  @Override
  public int compare(final WeightedWord w1, final WeightedWord w2) {
    final int delta = (int)((w1.weight - w2.weight) * 100);
    return (delta < 0) ? 1 : (delta > 1) ? -1 : w1.word.compareTo(w2.word);
  }

  public boolean contains(final String word, final double minWeight) {
    WeightedWord ww;
    for (int i = 0; i < words.size(); i++) {
      ww = words.get(i);
      if (ww.word.equals(word) && (ww.weight >= minWeight)) { return true; }
    }
    return false;
  }

  public void add(final MultipleWords newWords) {
    for (final WeightedWord w : newWords.words) {
      add(w.word, w.weight);
    }
  }

  public void addSpecial(String newValue, final double weight) {
    if (LibStr.isNotEmptyOrNull(newValue)) {
      if (newValue.startsWith(Helper.MINUS)) {
        newValue = newValue.substring(1);
        if (LibStr.isNotEmptyOrNull(newValue)) {
          add(newValue, -weight);
        }
      }
      else {
        add(newValue, weight);
      }
    }
  }

  public List<String> getWords() {
    final List<String> res = new ArrayList<>();
    for (final WeightedWord w : words) {
      res.add(w.word);
    }
    return res;
  }
}
