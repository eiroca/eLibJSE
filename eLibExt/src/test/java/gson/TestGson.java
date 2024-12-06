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
package gson;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.google.gson.JsonObject;
import net.eiroca.ext.library.gson.LibGson;
import net.eiroca.library.data.Tags;

public class TestGson {

  private static final String TEST1 = "{"
      + "    \"employees\": {"
      + "        \"John\": {"
      + "            \"age\": null,"
      + "            \"position\": \"developer\""
      + "        },\r\n"
      + "        \"Mary\": {"
      + "            \"age\": 25,"
      + "            \"position\": {roles: [\"designer\",\"CEO\"]}"
      + "        }"
      + "    }"
      + "}";

  private static final String TEST2 = "{\"a\":null,\"b\":[1,2],\"c c\":3}";

  @Test
  public void gsonTest1() {
    JsonObject o;
    o = LibGson.fromString(null);
    assertNull(o);
    o = LibGson.fromString("");
    assertNull(o);
    o = LibGson.fromString("yaml:");
    assertNull(o);
  }

  @Test
  public void gsonTest2() {
    JsonObject o;
    StringBuilder sb = new StringBuilder();
    o = LibGson.fromString(TEST1);
    assertTrue(o != null);
    LibGson.visit("", o, true, true, true, ".", (prefix, name, value) -> sb.append(prefix + name + " -> " + value));
    assertTrue(sb.toString().equals("employees.John.age -> nullemployees.John.position -> developeremployees.Mary.age -> 25employees.Mary.position.roles -> [\"designer\",\"CEO\"]"));
  }

  @Test
  public void gsonTest3() {
    JsonObject o;
    StringBuilder sb = new StringBuilder();
    o = LibGson.fromString(TEST2);
    assertTrue(o != null);
    LibGson.visit("", o, false, false, false, ".", (prefix, name, value) -> sb.append(prefix + name + " -> " + value));
    assertTrue(sb.toString().equals("c c -> 3"));
  }

  @Test
  public void gsonTest4() {
    Tags t;
    Map<String, String> mappedName = new HashMap<String, String>();
    mappedName.put("employees.John.position", "A");
    t = LibGson.getTags(TEST1, true, "X.", mappedName);
    assertTrue(t != null);
    assertTrue(t.toString().equals("tags={A=developer, X.employees.Mary.age=25}"));
    t = LibGson.getTags(TEST2, false, "", null);
    assertTrue(t != null);
    assertTrue(t.toString().equals("tags={c c=3}"));
  }

}
