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
package data;

import org.junit.Assert;
import org.junit.Test;
import net.eiroca.library.metrics.Measure;
import net.eiroca.library.metrics.Statistic;
import net.eiroca.library.metrics.datum.StatisticDatum;

public class TestMetrics {

  private static final double ZERO = 0.0000001;

  private void assertEqual(final double a, final double b) {
    Assert.assertTrue((a - b) < TestMetrics.ZERO);
  }

  @Test
  public void basicMeasure() {
    final Measure m = new Measure();
    m.setValue(1);
    m.addValue(3);
    assertEqual(m.getValue(), 4.0);
  }

  @Test
  public void basicStatistic() {
    final Statistic m = new Statistic();
    m.addValue(3);
    m.addValue(1);
    final StatisticDatum stat = m.getDatum();
    assertEqual(stat.getValue(), 2.0);
    assertEqual(stat.getAverage(), 2.0);
    assertEqual(stat.getStdDev(), 1.0);
    assertEqual(stat.getFirst(), 3.0);
    assertEqual(stat.getLast(), 1.0);
    assertEqual(stat.getMin(), 1.0);
    assertEqual(stat.getMax(), 3.0);
  }

  @Test
  public void measure2json() {
    final Measure m = new Measure();
    m.setValue(1);
    m.addValue(3);
    Measure s = m.getSplitting("Split1");
    s.setValue("A", 2);
    s.setValue("B", 2);
    s = m.getSplitting("Split2");
    s.setValue(4);
    final String expected = "{\"value\":4.0,\"splittings\":{\"Split1\":{\"splittings\":{\"A\":{\"value\":2.0},\"B\":{\"value\":2.0}}},\"Split2\":{\"value\":4.0}}}";
    final String json = m.toString();
    Assert.assertTrue(expected.equals(json));
  }

  @Test
  public void statistic2json() {
    final Statistic m = new Statistic();
    m.addValue(1);
    m.addValue(3);
    Statistic s = m.getSplitting("Split1");
    s.setValue("A", 2);
    s.setValue("B", 2);
    s = m.getSplitting("Split2");
    s.setValue(4);
    final String expected = "{\"value\":{\"count\":2,\"first\":1.0,\"last\":3.0,\"min\":1.0,\"max\":3.0,\"sumX\":4.0,\"sumX2\":10.0,\"sumX3\":28.0},\"splittings\":{\"Split1\":{\"splittings\":{\"A\":{\"value\":{\"count\":1,\"first\":2.0,\"last\":2.0,\"min\":2.0,\"max\":2.0,\"sumX\":2.0,\"sumX2\":4.0,\"sumX3\":8.0}},\"B\":{\"value\":{\"count\":1,\"first\":2.0,\"last\":2.0,\"min\":2.0,\"max\":2.0,\"sumX\":2.0,\"sumX2\":4.0,\"sumX3\":8.0}}}},\"Split2\":{\"value\":{\"count\":1,\"first\":4.0,\"last\":4.0,\"min\":4.0,\"max\":4.0,\"sumX\":4.0,\"sumX2\":16.0,\"sumX3\":64.0}}}}";
    final String json = m.toString();
    Assert.assertTrue(expected.equals(json));
  }

  @Test
  public void computedMeasure() {

  }

}
