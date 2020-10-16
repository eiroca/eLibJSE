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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Tags implements ITags {

  private String defaultTagValue = "1";
  private String tagFormat = "%s=%s";

  private Map<String, Object> tags = new TreeMap<>();

  public Tags() {
  }

  public Tags(final Tags t) {
    addTags(t);
  }

  public String getDefaultTagValue() {
    return defaultTagValue;
  }

  public void setDefaultTagValue(final String defaultTagValue) {
    if (defaultTagValue == null) { throw new IllegalArgumentException("Default tag value cannot be null"); }
    this.defaultTagValue = defaultTagValue;
  }

  public String getTagFormat() {
    return tagFormat;
  }

  public void setTagFormat(final String tagFormat) {
    if (tagFormat == null) { throw new IllegalArgumentException("Tag format value cannot be null"); }
    this.tagFormat = tagFormat;
  }

  @Override
  public Iterator<String> iterator() {
    return tags.keySet().iterator();
  }

  @Override
  public int size() {
    return tags.size();
  }

  @Override
  public boolean isEmpty() {
    return tags.isEmpty();
  }

  @Override
  public boolean contains(final Object o) {
    return tags.containsKey(o);
  }

  @Override
  public Object[] toArray() {
    return tags.keySet().toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return tags.keySet().toArray(a);
  }

  @Override
  public boolean add(final String name) {
    final boolean r = tags.containsKey(name);
    tags.put(name, null);
    return r;
  }

  @Override
  public boolean add(final String name, final Object value) {
    final boolean r = tags.containsKey(name);
    tags.put(name, value);
    return r;
  }

  @Override
  public boolean remove(final Object name) {
    final boolean r = tags.containsKey(name);
    if (r) {
      tags.remove(name);
    }
    return false;
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return tags.keySet().containsAll(c);
  }

  @Override
  public boolean addAll(final Collection<? extends String> c) {
    if (c instanceof Tags) { return addTags((Tags)c); }
    final int s = tags.size();
    for (final String e : c) {
      add(e);
    }
    return tags.size() != s;
  }

  @Override
  public boolean addTags(final Tags t) {
    final int s = tags.size();
    if (t.size() > 0) {
      final Iterator<Entry<String, Object>> x = t.namedIterator();
      while (x.hasNext()) {
        final Entry<String, Object> e = x.next();
        add(e.getKey(), e.getValue());
      }
    }
    return tags.size() != s;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    final int s = tags.size();
    final Map<String, Object> newTags = new HashMap<>();
    for (final Object e : c) {
      if ((e != null) && tags.containsKey(e)) {
        final Object v = tags.get(e);
        newTags.put(e.toString(), v);
      }
    }
    tags = newTags;
    return tags.size() != s;
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    final int s = tags.size();
    for (final Object e : c) {
      remove(e);
    }
    return tags.size() != s;
  }

  @Override
  public void clear() {
    tags.clear();
  }

  @Override
  public Iterator<Entry<String, Object>> namedIterator() {
    return tags.entrySet().iterator();
  }

  public List<String> getTags() {
    return getTags(true);
  }

  @Override
  public boolean hasValue(final String name, final boolean withVal) {
    return tags.containsKey(name) && (tags.get(name) != null);
  }

  @Override
  public String get(final String name, final boolean withVal) {
    if (tags.containsKey(name)) {
      if (withVal) {
        Object value = tags.get(name);
        if (value == null) {
          value = defaultTagValue;
        }
        return String.format(tagFormat, name, value.toString());
      }
      return name;
    }
    return null;
  }

  @Override
  public List<String> getTags(final boolean withVal) {
    final List<String> result = new ArrayList<>();
    if (tags.size() > 0) {
      for (final Entry<String, Object> e : tags.entrySet()) {
        final String name = e.getKey();
        Object value = e.getValue();
        if (value == null) {
          value = defaultTagValue;
        }
        result.add(withVal ? String.format(tagFormat, name, value) : name);
      }
    }
    return result;
  }

  public boolean tagValueIs(final String name, final String value) {
    final Object v = tags.get(name);
    return v != null ? v.equals(value) : value == null;
  }

  public Object tagValue(final String name) {
    return tags.get(name);
  }

}
