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
package net.eiroca.library.metrics;

import java.util.ArrayList;
import java.util.Collection;
import net.eiroca.library.metrics.datum.StatisticDatum;

public class Statistic extends Metric<Statistic, StatisticDatum> {

  public Statistic() {
    this((MetricMetadata)null);
  }

  public Statistic(final String name) {
    this(new MetricMetadata(name));
  }

  public Statistic(final MetricMetadata metadata) {
    this.metadata = metadata;
    datum = newDatum();
  }

  @Override
  public StatisticDatum newDatum() {
    return new StatisticDatum();
  }

  @Override
  public Statistic newSplit(final String name) {
    return new Statistic();
  }

  public Collection<Statistic> getSplitMeasures() {
    if (splittings == null) { return null; }
    final ArrayList<Statistic> result = new ArrayList<>();
    for (final IMetric<StatisticDatum> x : splittings.values()) {
      result.add((Statistic)x);
    }
    return result;
  }

}
