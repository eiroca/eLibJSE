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
package data;

import static java.lang.String.valueOf;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import net.eiroca.library.data.DynamicData;

public class TestDynamicData {

  public class TestObject extends DynamicData {

    public String fieldS;
    public Integer fieldI;

  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testAccess() {
    TestObject obj = new TestObject();
    obj.set("x", "x");
    obj.set("fieldS", "S");
    obj.set("fieldI", 1);
    Assert.assertEquals(obj.get("x"), "x");
    Assert.assertEquals(obj.fieldS, "S");
    Assert.assertEquals(String.valueOf(obj.fieldI), valueOf(1));
    obj.removeExt("x");
    Assert.assertEquals(obj.get("x", "?"), "?");
  }

  @Test
  public void testCopy() throws Exception {
    TestObject obj = new TestObject();
    obj.set("x", "x");
    obj.set("fieldS", "S");
    obj.set("fieldI", 1);
    obj.removeExt("x");
    DynamicData copy = new DynamicData();
    copy.assign(obj);
    String x = copy.get("x", "?");
    String s = copy.get("fieldS");
    Integer i = copy.get("fieldI");
    Assert.assertEquals(x, "?");
    Assert.assertEquals(s, "S");
    Assert.assertTrue(i == 1);
  }

}
