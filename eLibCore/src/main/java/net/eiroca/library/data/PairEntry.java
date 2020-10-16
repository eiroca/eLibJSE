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

import java.util.Map.Entry;

public class PairEntry<L, R> implements Entry<L, R> {

  private final L left;
  private R right;

  public PairEntry(final L left, final R right) {
    this.left = left;
    this.right = right;
  }

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }

  @Override
  public int hashCode() {
    return left.hashCode() ^ right.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof PairEntry)) { return false; }
    final PairEntry<?, ?> pairo = (PairEntry<?, ?>)o;
    return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
  }

  @Override
  public L getKey() {
    return left;
  }

  @Override
  public R getValue() {
    return right;
  }

  @Override
  public R setValue(final R value) {
    right = value;
    return right;
  }

}
