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
package net.eiroca.sysadm.diagnostics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import net.eiroca.library.diagnostics.monitors.ApacheServerMonitor;

public class TestModStatus {

  public static String readFile(final String path) {
    String result = null;
    try {
      final BufferedReader br = new BufferedReader(new FileReader(path));
      try {
        final StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
          sb.append(line);
          sb.append(System.lineSeparator());
          line = br.readLine();
        }
        result = sb.toString();
      }
      finally {
        br.close();
      }
    }
    catch (final FileNotFoundException e) {
      System.err.println(e.getMessage());
    }
    catch (final IOException e) {
    }
    return result;
  }

  public static void main(final String[] args) {
    final String data = TestModStatus.readFile("C:\\Users\\ecroce\\workspace\\DynaTrace\\DynaTrace-AdvancedServerMonitor-Plugin\\testdata\\modstatus.out");
    final ApacheServerMonitor checker = new ApacheServerMonitor();
    checker.parseModStatus(data);
    System.out.println(checker);
  }
}
