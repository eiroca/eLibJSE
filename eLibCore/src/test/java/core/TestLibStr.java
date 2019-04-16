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
package core;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import net.eiroca.library.core.LibStr;

public class TestLibStr {

  @Test
  public void splitTest1() {
    List<String> res;
    res = LibStr.split("", ',', '\"');
    Assert.assertTrue(res.size() == 1);
    Assert.assertTrue(res.get(0).equals(""));
  }

  @Test
  public void splitTest2() {
    List<String> res;
    res = LibStr.split("A,B,C,", ',', '\"');
    Assert.assertTrue(res.size() == 4);
    Assert.assertTrue(res.get(0).equals("A"));
    Assert.assertTrue(res.get(1).equals("B"));
    Assert.assertTrue(res.get(2).equals("C"));
    Assert.assertTrue(res.get(3).equals(""));
  }

  @Test
  public void splitTest3() {
    List<String> res;
    res = LibStr.split("\"A,B,C\",", ',', '\"');
    Assert.assertTrue(res.size() == 2);
    Assert.assertTrue(res.get(0).equals("A,B,C"));
    Assert.assertTrue(res.get(1).equals(""));
  }

  @Test
  public void splitTest4() {
    List<String> res;
    res = LibStr.split("=\"10\"\"x10\"\"\",1", ',', '\"');
    Assert.assertTrue(res.size() == 2);
    Assert.assertTrue(res.get(0).equals("=10\"x10\""));
    Assert.assertTrue(res.get(1).equals("1"));
  }

  @Test
  public void listSplitTest() {
    final String val = "     A     C       B  ";
    List<String> c = LibStr.getList(val, -1);
    Assert.assertTrue((c != null) && (c.size() == 3));
    c = LibStr.getList(val, 3);
    Assert.assertTrue((c != null) && (c.size() == 3));
    c = LibStr.getList(val, 4);
    Assert.assertTrue((c == null));
    c = LibStr.getList(null, 1);
    Assert.assertTrue((c == null));
    c = LibStr.getList("A", 0);
    Assert.assertTrue((c == null));
  }

  @Test
  public void listSplitSepTest() {
    final String val = ",A,,C,B  ";
    List<String> c = LibStr.getList(val, ',', -1);
    Assert.assertTrue((c != null) && (c.size() == 5));
    c = LibStr.getList(val, ',', 3);
    Assert.assertTrue((c != null) && (c.size() == 3));
    c = LibStr.getList(null, ',', 1);
    Assert.assertTrue((c == null));
    c = LibStr.getList("A", ',', 0);
    Assert.assertTrue((c == null));
  }
}
