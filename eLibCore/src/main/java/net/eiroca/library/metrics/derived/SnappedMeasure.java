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
package net.eiroca.library.metrics.derived;

import java.util.Map.Entry;
import net.eiroca.library.metrics.IMetric;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.MetricMetadata;
import net.eiroca.library.metrics.datum.IDatum;
import net.eiroca.library.metrics.util.MeasureSnapshot;
import net.eiroca.library.metrics.util.SnapshotStorage;

public abstract class SnappedMeasure extends Measure implements IDerivedMeasure {

  public Measure observed;

  public SnappedMeasure(final Measure observed) {
    this(null, observed);
  }

  public SnappedMeasure(final MetricMetadata metadata, final Measure observed) {
    super(metadata);
    this.observed = observed;
  }

  @Override
  public void refresh() {
    super.reset();
    final MeasureSnapshot oldSnap = SnapshotStorage.get(id);
    final MeasureSnapshot newSnap = SnapshotStorage.put(id, new MeasureSnapshot(observed));
    if (oldSnap != null) {
      iterate(this, newSnap, oldSnap);
    }
  }

  protected void iterate(final IMetric<?> dest, final MeasureSnapshot newSnap, final MeasureSnapshot oldSnap) {
    update(dest.getDatum(), newSnap.datum, oldSnap.datum);
    if (newSnap.hasSplittings()) {
      for (final Entry<String, MeasureSnapshot> ms : newSnap.splittings.entrySet()) {
        final String splitName = ms.getKey();
        final MeasureSnapshot newSplit = ms.getValue();
        final MeasureSnapshot oldSplit = oldSnap.splittings.get(splitName);
        if (oldSplit != null) {
          final IMetric<?> dms = dest.getSplitting(splitName);
          iterate(dms, newSplit, oldSplit);
        }
      }
    }
  }

  abstract protected void update(IDatum dest, IDatum newDatum, IDatum oldDatum);

}
