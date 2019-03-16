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
package config;

import org.junit.Assert;
import org.junit.Test;
import net.eiroca.library.config.Parameters;
import net.eiroca.library.config.parameter.IntegerParameter;
import net.eiroca.library.config.parameter.StringParameter;

public class TestParameters {

  public class Config1 {

    public String f1;
    public String x = "0";

  }

  public class Cofing2 extends Config1 {

    public int f2;
    public int x;
    private final int p = -1;

  }

  Parameters params = new Parameters();
  StringParameter p1 = new StringParameter(params, "f1", "1");
  IntegerParameter p2 = new IntegerParameter(params, "f2", 2);
  IntegerParameter p3 = new IntegerParameter(params, "x", 3);
  IntegerParameter p4 = new IntegerParameter(params, "p", 4);

  @Test
  public void configTest1() {
    Assert.assertEquals(p1.get(), "1");
    Assert.assertEquals((long)p2.get(), 2);
    Assert.assertEquals((long)p3.get(), 3);
    Assert.assertEquals((long)p4.get(), 4);
  }

  @Test
  public void configTest2() {
    final Cofing2 config = new Cofing2();
    params.saveConfig(config);
    Assert.assertEquals(config.f1, "1");
    Assert.assertEquals(config.f2, 2);
    Assert.assertEquals(config.x, 3);
    Assert.assertEquals(config.p, -1);
  }

}
