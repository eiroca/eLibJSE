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

  public class Cofing3 extends Config1 {

    public int a = 1;
    int _a = 2;
    private final int _b = 3;
    protected int _c = 4;
    public int _d = 5;

    public int getA() {
      return _a;
    }

    public int getB() {
      return _b;
    }

    public int getC() {
      return _c;
    }

    public int getD() {
      return _d;
    }

  }

  Parameters params = new Parameters();
  StringParameter p1 = new StringParameter(params, "f1", "1");
  IntegerParameter p2 = new IntegerParameter(params, "f2", 2);
  IntegerParameter p3 = new IntegerParameter(params, "x", 3);
  IntegerParameter p4 = new IntegerParameter(params, "p", 4);

  Parameters params2 = new Parameters();
  IntegerParameter pa1 = new IntegerParameter(params2, "a", 0);
  IntegerParameter pa2 = new IntegerParameter(params2, "b", 0);
  IntegerParameter pa3 = new IntegerParameter(params2, "c", 0);
  IntegerParameter pa4 = new IntegerParameter(params2, "d", 0);

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

  @Test
  public void configTest3() {
    final Cofing3 config = new Cofing3();
    params2.saveConfig(config, "_", true);
    Assert.assertEquals(config.a, 1);
    Assert.assertEquals(config.getA(), 0);
    Assert.assertEquals(config.getB(), 0);
    Assert.assertEquals(config.getC(), 0);
    Assert.assertEquals(config.getD(), 0);
  }

}
