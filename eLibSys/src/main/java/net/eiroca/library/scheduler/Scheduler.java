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
package net.eiroca.library.scheduler;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import net.eiroca.library.system.Logs;

public class Scheduler implements AutoCloseable {

  protected Logger logger = Logs.getLogger();
  protected final SchedulerThread schedulerThread;
  protected boolean active;

  protected ExecutorService tasksExecutor;

  public Scheduler() {
    this(Executors.newFixedThreadPool(8), 1, 60, TimeUnit.SECONDS, "Scheduler");
  }

  public Scheduler(final ExecutorService tasksExecutor, final int minCheckInterval, final int maxCheckInterval, final TimeUnit timeUnit, final String schedulerName) {
    schedulerThread = new SchedulerThread(this, minCheckInterval, maxCheckInterval, timeUnit, schedulerName);
    this.tasksExecutor = tasksExecutor;
  }

  public Task addTask(final Runnable task, final SchedulerPolicy policy) {
    final String id = UUID.randomUUID().toString();
    final Task t = new Task(this, id, task, policy);
    schedulerThread.add(t);
    return t;
  }

  public void removeTask(final String id) {
    schedulerThread.remove(id);
  }

  public void start() {
    active = true;
    schedulerThread.start();
  }

  public void stop() {
    close();
  }

  @Override
  public void close() {
    active = false;
    schedulerThread.close();
    schedulerThread.wakeup();
    tasksExecutor.shutdown();
  }

  public void add(final Task task) {
    schedulerThread.add(task);
    // Wake-up sleeping state
    schedulerThread.wakeup();
  }

  public void remove(final String id) {
    schedulerThread.remove(id);
  }

  public boolean isActive() {
    return active;
  }

  public void execute(final Runnable task) {
    if (active) {
      tasksExecutor.execute(task);
    }
  }

  public void onBeforeChecking() {
  }

  public void onBeforeExecuting() {
  }

  public void onBeforeSleeping() {
  }

  public void onTaskStart(final Task task) {
  }

  public void onTaskEnd(final Task task) {
    final long nextRun = task.nextRun();
    if (nextRun < schedulerThread.getNextRun()) {
      schedulerThread.wakeup();
    }
  }

  public void onTaskSkip(final Task task) {
    logger.debug("onTaskSkip " + task.getId());
  }

  public void onTaskError(final Task task, final Exception e) {
    logger.warn("onTaskError " + task.getId() + " -> " + e);
  }

}
