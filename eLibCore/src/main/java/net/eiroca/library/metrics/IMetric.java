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
package net.eiroca.library.metrics;

import java.util.Map;
import java.util.UUID;
import net.eiroca.library.data.Tags;
import net.eiroca.library.metrics.datum.IDatum;

public interface IMetric<D extends IDatum> extends IDatum {

  public void reset();

  public UUID getId();

  public D newDatum();

  public D getDatum();

  public MetricMetadata getMetadata();

  public Tags getTags();

  public boolean hasSplittings();

  public Map<String, IMetric<D>> getSplittings();

  public IMetric<D> getSplitting(final String splitName);

  public IMetric<D> getSplitting(final String... splitNames);

  public void toJson(StringBuilder sb);

}
