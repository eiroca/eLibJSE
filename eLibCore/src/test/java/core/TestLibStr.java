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

import org.junit.Assert;
import org.junit.Test;
import net.eiroca.library.core.LibStr;

public class TestLibStr {

  @Test
  public void lowerTest() {
    String res;
    res = LibStr.lower(null);
    Assert.assertTrue(res == null);
    res = LibStr.lower("AbC");
    Assert.assertTrue(res.equals("abc"));
  }

}
