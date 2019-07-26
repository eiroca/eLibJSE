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
package net.eiroca.library.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SchedulerThread extends Thread {

  private final Scheduler scheduler;
  private final int minDelay;
  private final int maxDelay;
  private final AtomicReference<SchedulerState> state = new AtomicReference<>();
  private final Object lock = new Object();
  private final List<Task> tasks = new ArrayList<>();
  private long nextRun;

  public SchedulerThread(final Scheduler scheduler) {
    this(scheduler, 0, 60, TimeUnit.SECONDS, "SchedulerThread");
  }

  public SchedulerThread(final Scheduler scheduler, final int minDelay, final int maxDelay, final TimeUnit timeUnit, final String threadName) {
    if (minDelay < 0) { throw new IllegalArgumentException("minDelay < 0. Value: " + minDelay); }
    if (maxDelay < minDelay) { throw new IllegalArgumentException("maxDelay < minDelay. Value: " + maxDelay); }
    if (timeUnit == null) { throw new IllegalArgumentException("timeUnit is null"); }
    this.scheduler = scheduler;
    this.minDelay = (int)timeUnit.toMillis(minDelay);
    this.maxDelay = (int)timeUnit.toMillis(maxDelay);
    setName(threadName);
    setDaemon(true);
    state.set(SchedulerState.IDLE);
  }

  @Override
  public void run() {
    while (scheduler.isActive()) {
      try {
        final long now = System.currentTimeMillis();
        nextRun = taskCheck(now);
        int delay = (int)(nextRun - now);
        if (delay > maxDelay) {
          delay = maxDelay;
        }
        else if (delay < minDelay) {
          delay = minDelay;
        }
        if ((delay > 10) && (state.get() == SchedulerState.IDLE)) {
          scheduler.onBeforeSleeping();
          synchronized (lock) {
            state.set(SchedulerState.SLEEPING);
            try {
              lock.wait(delay);
            }
            catch (final InterruptedException e) {
            }
          }
        }
        state.set(SchedulerState.IDLE);
      }
      catch (final Exception e) {
        System.err.println("Internal error: " + e);
      }
    }
  }

  public long taskCheck(final long currentMillis) {
    long minNextRun = Long.MAX_VALUE;
    final List<Task> tasksToRun = new ArrayList<>();
    if (state.weakCompareAndSet(SchedulerState.IDLE, SchedulerState.CHECKING)) {
      scheduler.onBeforeChecking();
      synchronized (tasks) {
        for (final Task t : tasks) {
          if (t.isBusy()) {
            scheduler.onTaskSkip(t);
            continue;
          }
          final long nextRun = t.nextRun();
          if (nextRun < minNextRun) {
            minNextRun = nextRun;
          }
          if (nextRun <= currentMillis) {
            tasksToRun.add(t);
          }
        }
      }
    }
    if (state.weakCompareAndSet(SchedulerState.CHECKING, SchedulerState.EXECURTING)) {
      scheduler.onBeforeExecuting();
      for (final Task t : tasksToRun) {
        try {
          t.start();
        }
        catch (final RejectedExecutionException e) {
          t.setErrorState(e);
        }
      }
    }
    if (!state.weakCompareAndSet(SchedulerState.EXECURTING, SchedulerState.IDLE)) {
      minNextRun = Long.MAX_VALUE;
    }
    return (minNextRun == Long.MAX_VALUE) ? currentMillis + minDelay : minNextRun;
  }

  public void add(final Task task) {
    synchronized (tasks) {
      tasks.add(task);
    }
  }

  public void remove(final String id) {
    synchronized (tasks) {
      for (final Task task : tasks) {
        if (task.getId().equals(id)) {
          tasks.remove(task);
          break;
        }
      }
    }
  }

  public void wakeup() {
    if (state.get() == SchedulerState.SLEEPING) {
      try {
        lock.notify();
      }
      catch (final IllegalStateException | IllegalMonitorStateException e) {
      }
    }
  }

  public void close() {
    state.set(SchedulerState.IDLE);
  }

  public SchedulerState getShedulerState() {
    return state.get();
  }

  public long getNextRun() {
    return nextRun;
  }

  public List<Task> getTaskList() {
    return tasks;
  }

}
