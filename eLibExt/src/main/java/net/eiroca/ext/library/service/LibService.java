/**
 *
 * Copyright (C) 1999-2019 Enrico Croce - AGPL >= 3.0
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
package net.eiroca.ext.library.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.eiroca.library.system.Logs;

public class LibService {

  transient private static final Logger logger = Logs.getLogger();

  public static void stopServices(final ExecutorService... services) {
    for (final ExecutorService service : services) {
      try {
        if (service != null) {
          service.shutdown();
          if (!service.awaitTermination(1, TimeUnit.SECONDS)) {
            service.shutdownNow();
          }
        }
      }
      catch (final InterruptedException e) {
        LibService.logger.info("Interrupted while awaiting termination", e);
      }
    }
  }

  public static ScheduledExecutorService startService(final Runnable task, final long initialDelayMS, final long delayMS) {
    final String name = task.getClass().getSimpleName();
    final ScheduledExecutorService result = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(name).build());
    result.scheduleWithFixedDelay(task, initialDelayMS, delayMS, TimeUnit.MILLISECONDS);
    return result;
  }

}
