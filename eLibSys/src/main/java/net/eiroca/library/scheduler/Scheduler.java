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

  Logger logger = Logs.getLogger();

  private final SchedulerThread schedulerThread;
  private boolean active;

  final ExecutorService tasksExecutor;

  public Scheduler() {
    this(Executors.newFixedThreadPool(8), 1, TimeUnit.SECONDS, "Scheduler");
  }

  public Scheduler(final ExecutorService tasksExecutor, final int minCheckInterval, final TimeUnit timeUnit, final String schedulerName) {
    schedulerThread = new SchedulerThread(this, minCheckInterval, timeUnit, schedulerName);
    this.tasksExecutor = tasksExecutor;
  }

  public String addTask(final Runnable task, final SchedulerPolicy policy) {
    final String id = UUID.randomUUID().toString();
    schedulerThread.add(new Task(this, id, task, policy));
    return id;
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
    logger.trace("onBeforeChecking");
  }

  public void onBeforeExecuting() {
    logger.trace("onBeforeExecuting");
  }

  public void onBeforeSleeping() {
    logger.trace("onBeforeSleeping");
  }

  public void onTaskStart(final Task task) {
    logger.trace("onTaskStart " + task.getId());
  }

  public void onTaskEnd(final Task task) {
    logger.trace("onTaskEnd " + task.getId());
  }

  public void onTaskSkip(final Task task) {
    logger.trace("onTaskSkip " + task.getId());
  }

  public void onTaskError(final Task task, final Exception e) {
    logger.warn("onTaskError " + task.getId() + " -> " + e);
  }

}
