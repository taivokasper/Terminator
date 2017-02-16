package com.github.taivokasper.executor.shutdowner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

class BlockingShutdownHelper implements ShutdownHelper {
  private final List<ExecutorWrapper> executorWrappers;

  BlockingShutdownHelper() {
    executorWrappers = new ArrayList<ExecutorWrapper>();
  }

  @Override
  public synchronized ShutdownHelper addShutdownItem(ExecutorService executorService) {
    executorWrappers.add(new ExecutorWrapper(executorService, 0, SECONDS));
    return this;
  }

  @Override
  public synchronized ShutdownHelper addShutdownItem(ExecutorService executorService, long time, TimeUnit timeUnit) {
    executorWrappers.add(new ExecutorWrapper(executorService, time, timeUnit));
    return this;
  }

  // TODO check from https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html on how to properly shutdown
  @Override
  public synchronized void terminate() throws InterruptedException {
    List<ExecutorWrapper> executorWrappers = new ArrayList<ExecutorWrapper>(this.executorWrappers);
    Collections.sort(executorWrappers, new Comparator<ExecutorWrapper>() {
      @Override
      public int compare(ExecutorWrapper e1, ExecutorWrapper e2) {
        return Long.valueOf(e1.timeUnit.toNanos(e1.time)).compareTo(e2.timeUnit.toNanos(e2.time));
      }
    });

    ExecutorWrapper longestTimeoutExecutor = executorWrappers.get(executorWrappers.size() - 1);
    long timeLeft = longestTimeoutExecutor.timeUnit.toNanos(longestTimeoutExecutor.time);

    for (ExecutorWrapper executorWrapper : executorWrappers) {
      executorWrapper.executorService.shutdown();
    }

    for (ExecutorWrapper executorWrapper : executorWrappers) {
      if (timeLeft > 0) {
        long minAwait = Math.min(timeLeft, executorWrapper.timeUnit.toNanos(executorWrapper.time));
        long start = System.nanoTime();
        executorWrapper.executorService.awaitTermination(minAwait, NANOSECONDS);
        timeLeft -= (System.nanoTime() - start);
      }
      executorWrapper.executorService.shutdownNow();
    }

    if (timeLeft > 0) {
      for (ExecutorWrapper executorWrapper : executorWrappers) {
        if (timeLeft <= 0) {
          break;
        }
        long minAwait = Math.min(timeLeft, executorWrapper.timeUnit.toNanos(executorWrapper.time));
        long start = System.nanoTime();
        executorWrapper.executorService.awaitTermination(minAwait, NANOSECONDS);
        timeLeft -= (System.nanoTime() - start);
      }
    }
  }
}