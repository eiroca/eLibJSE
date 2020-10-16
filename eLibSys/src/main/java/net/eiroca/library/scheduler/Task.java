/**
 *
 * Copyright (C) 1999-2020 Enrico Croce - AGPL >= 3.0
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

import java.util.Date;
import net.eiroca.library.data.Status;

public class Task implements Runnable {

  public static final Status STATUS_IDLE = new Status(0, "IDLE");
  public static final Status STATUS_OK = new Status(1, "EXECUTED");
  public static final Status STATUS_KO = new Status(-1, "FAILED");

  enum TaskState {
    IDLE, WAITING, EXECUTING
  }

  Scheduler scheduler;
  final Runnable job;
  final SchedulerPolicy schedulerPolicy;

  String id;
  String name;
  TaskState executionState;
  Status status = Task.STATUS_IDLE;
  long lastRun;
  long nextRun;

  public Task(final Scheduler scheduler, final String id, final Runnable task, final SchedulerPolicy schedulerPolicy) {
    this.scheduler = scheduler;
    this.id = id;
    job = task;
    this.schedulerPolicy = schedulerPolicy;
    executionState = TaskState.IDLE;
    lastRun = System.currentTimeMillis();
    nextRun = schedulerPolicy.next(lastRun);
  }

  @Override
  public String toString() {
    return "Task [id=" + id + ", executionState=" + executionState + ", status=" + status + ", nextRun=" + new Date(nextRun) + "]";
  }

  public Runnable getJob() {
    return job;
  }

  public String getId() {
    return id;
  }

  public boolean isBusy() {
    return executionState != TaskState.IDLE;
  }

  public TaskState getState() {
    return executionState;
  }

  public long getLastRun() {
    return lastRun;
  }

  public long nextRun() {
    return nextRun;
  }

  @Override
  public void run() {
    executionState = TaskState.EXECUTING;
    try {
      job.run();
      status = Task.STATUS_OK;
    }
    catch (final Exception e) {
      setErrorState(e);
      throw e;
    }
    finally {
      executionState = TaskState.IDLE;
      lastRun = System.currentTimeMillis();
      nextRun = schedulerPolicy.next(lastRun);
      scheduler.onTaskEnd(this);
    }
  }

  public void start() {
    if (executionState == TaskState.IDLE) {
      scheduler.onTaskStart(this);
      executionState = TaskState.WAITING;
      scheduler.execute(this);
    }
  }

  public void setErrorState(final Exception e) {
    status = Task.STATUS_KO;
    scheduler.onTaskError(this, e);
  }

  public String getName() {
    return (name != null) ? name : id;
  }

  public void setName(final String name) {
    this.name = name;
  }

}
