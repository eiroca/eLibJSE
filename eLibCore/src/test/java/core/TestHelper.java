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
package core;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;
import net.eiroca.library.core.Helper;

public class TestHelper {

  @Test
  public void concatenateTest() {
    final String expexted = "OK";
    final StringBuilder concatenated = new StringBuilder();
    Helper.concatenate(concatenated, null, "", "OK", "", null);
    Assert.assertEquals(expexted, concatenated.toString());
  }

  @Test
  public void propertiesTest() {
    Properties a = Helper.buildProperties("A=1");
    Properties b = Helper.buildProperties("B=1\nA=2");
    Properties c = Helper.mergeProperties(a, new Properties[] {
        b
    }, null);
    Assert.assertTrue(c != null);
    Assert.assertTrue(c.size() == 2);
    Assert.assertTrue(c.getProperty("A").equals("1"));
    c = Helper.mergeProperties(b, new Properties[] {
        a
    }, null);
    Assert.assertTrue(c != null);
    Assert.assertTrue(c.size() == 2);
    Assert.assertTrue(c.getProperty("A").equals("2"));
  }

}
