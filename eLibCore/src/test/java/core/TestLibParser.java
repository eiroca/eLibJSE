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

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import net.eiroca.library.core.LibParser;
import net.eiroca.library.data.Tags;

public class TestLibParser {

  @Test
  public void splitTest1() {
    List<String> res;
    res = LibParser.split("", ',', '\"');
    Assert.assertTrue(res.size() == 1);
    Assert.assertTrue(res.get(0).equals(""));
  }

  @Test
  public void splitTest2() {
    List<String> res;
    res = LibParser.split("A,B,C,", ',', '\"');
    Assert.assertTrue(res.size() == 4);
    Assert.assertTrue(res.get(0).equals("A"));
    Assert.assertTrue(res.get(1).equals("B"));
    Assert.assertTrue(res.get(2).equals("C"));
    Assert.assertTrue(res.get(3).equals(""));
  }

  @Test
  public void splitTest3() {
    List<String> res;
    res = LibParser.split("\"A,B,C\",", ',', '\"');
    Assert.assertTrue(res.size() == 2);
    Assert.assertTrue(res.get(0).equals("A,B,C"));
    Assert.assertTrue(res.get(1).equals(""));
  }

  @Test
  public void splitTest4() {
    List<String> res;
    res = LibParser.split("=\"10\"\"x10\"\"\",1", ',', '\"');
    Assert.assertTrue(res.size() == 2);
    Assert.assertTrue(res.get(0).equals("=10\"x10\""));
    Assert.assertTrue(res.get(1).equals("1"));
  }

  @Test
  public void splitSpaces() {
    final String val = "     A     C       B  ";
    List<String> c = LibParser.splitWithSpaces(val, -1);
    Assert.assertTrue((c != null) && (c.size() == 3));
    c = LibParser.splitWithSpaces(val, 3);
    Assert.assertTrue((c != null) && (c.size() == 3));
    c = LibParser.splitWithSpaces(val, 4);
    Assert.assertTrue((c == null));
    c = LibParser.splitWithSpaces(null, 1);
    Assert.assertTrue((c == null));
    c = LibParser.splitWithSpaces("A", 0);
    Assert.assertTrue((c == null));
  }

  @Test
  public void splitSeparator() {
    final String val = ",A,,C,B  ";
    List<String> c = LibParser.splitWithSep(val, ',', -1);
    Assert.assertTrue((c != null) && (c.size() == 5));
    c = LibParser.splitWithSep(val, ',', 3);
    Assert.assertTrue((c != null) && (c.size() == 3));
    c = LibParser.splitWithSep(null, ',', 1);
    Assert.assertTrue((c == null));
    c = LibParser.splitWithSep("A", ',', 0);
    Assert.assertTrue((c == null));
  }

  @Test
  public void splitSeparator2() {
    final String val = "A|B|C|D|E||";
    List<String> c = LibParser.splitWithSep(val, '|', -1);
    Assert.assertTrue(c != null);
    Assert.assertTrue(c.size() == 7);
    Assert.assertTrue(c.get(0).equals("A"));
    Assert.assertTrue(c.get(1).equals("B"));
    Assert.assertTrue(c.get(2).equals("C"));
    Assert.assertTrue(c.get(3).equals("D"));
    Assert.assertTrue(c.get(4).equals("E"));
    Assert.assertTrue(c.get(5).equals(""));
    Assert.assertTrue(c.get(6).equals(""));
  }
  

  @Test
  public void splitWebLog() {
    final String val = "abc 123 - \"abc \"\"123\"\"\" [1/1/2000 10:00] \"zzz\" 1 \"\\\"A\\\"\" -1";
    List<String> c = LibParser.splitWebLog(val);
    Assert.assertTrue(c != null);
    Assert.assertTrue(c.size() == 9);
    Assert.assertTrue(c.get(0).equals("abc"));
    Assert.assertTrue(c.get(1).equals("123"));
    Assert.assertTrue(c.get(2).equals(""));
    Assert.assertTrue(c.get(3).equals("abc \"123\""));
    Assert.assertTrue(c.get(4).equals("1/1/2000 10:00"));
    Assert.assertTrue(c.get(5).equals("zzz"));
    Assert.assertTrue(c.get(6).equals("1"));
    Assert.assertTrue(c.get(7).equals("\"A\""));
    Assert.assertTrue(c.get(8).equals("-1"));
    c = LibParser.splitWebLog(null);
    Assert.assertTrue((c == null));
  }

  @Test
  public void splitAltLog() {
    List<String> c;
    c = LibParser.splitAltLog(null, 3, ' ', '[', ']');
    Assert.assertTrue((c == null));
    final String val = "field1 field2 [tag1] [tag[2]] [tag3] field3 last field";
    // output list is: "filed1" "field2" "tag1" "tag[2]" "tag3" "field3" "last field"
    c = LibParser.splitAltLog(val, 3, ' ', '[', ']');
    Assert.assertTrue(c != null);
    Assert.assertTrue(c.size() == 7);
    Assert.assertTrue(c.get(0).equals("field1"));
    Assert.assertTrue(c.get(1).equals("field2"));
    Assert.assertTrue(c.get(2).equals("tag1"));
    Assert.assertTrue(c.get(3).equals("tag[2]"));
    Assert.assertTrue(c.get(4).equals("tag3"));
    Assert.assertTrue(c.get(5).equals("field3"));
    Assert.assertTrue(c.get(6).equals("last field"));
  }

  private void check(String row, String[] output) {
    List<String> c = LibParser.splitOptAndMessage(row, ' ', ':', '-');
    Assert.assertTrue(c != null);
    Assert.assertTrue(c.size() == output.length);
    for (int i = 0; i < output.length; i++) {
      Assert.assertTrue(c.get(i).equals(output[i]));
    }
  }

  @Test
  public void splitOptMessage() {
    List<String> c;
    c = LibParser.splitOptAndMessage(null, ' ', ':', '-');
    Assert.assertTrue((c == null));
    check("opt : message", new String[] {
        "opt", "message"
    });
    check("opt - message", new String[] {
        "opt", "message"
    });
    check("opt: message", new String[] {
        "opt", "message"
    });
    check("opt- message", new String[] {
        "opt", "message"
    });
    check(": message", new String[] {
        "", "message"
    });
    check("mess:a: g e", new String[] {
        "mess:a: g e"
    });
    check("  message", new String[] {
        "message"
    });
  }

  //[tagValue1] [tagValue2] [tagA: tagValue[]] [tagB: tagValue] messag[e]
  //Names: "F1", "F2"
  //output: "5:messag[e]", "F1:tagValue1", "F2:tagValue2", "tagA:tagValue[]", "tagB:tagValue" 
  //[ ] [ a : x ]
  //output: "a:x", "F1:"
  // fields are ordered
  // null in -> null out
  private static final String[] NAMES = {
      "F1", "F2"
  };

  private void checkTags(String row, String[] output) {
    Tags c = LibParser.splitTagAndMessage(row, ' ', '[', ']', ':', NAMES);
    Assert.assertTrue(c != null);
    c.setTagFormat("%s:%s");
    List<String> vals = c.getTags();
    Assert.assertTrue(vals.size() == output.length);
    for (int i = 0; i < output.length; i++) {
      Assert.assertTrue(vals.get(i).equals(output[i]));
    }
  }

  @Test
  public void splitTagAndMessage() {
    Tags c;
    c = LibParser.splitTagAndMessage(null, ' ', '[', ']', ':', NAMES);
    Assert.assertTrue((c == null));
    checkTags("[ ] [ a : x ]", new String[] {
        "F1:", "a:x"
    });
    checkTags("[tagValue1] [tagValue2] [tagA: tagValue[]] [tagB: tagValue ] messag[e]", new String[] {
        "5:messag[e]",
        "F1:tagValue1",
        "F2:tagValue2",
        "tagA:tagValue[]",
        "tagB:tagValue"
    });
  }
}
