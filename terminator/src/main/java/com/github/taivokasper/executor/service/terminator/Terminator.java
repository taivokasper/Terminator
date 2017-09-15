package com.github.taivokasper.executor.service.terminator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

class Terminator implements UnequalTerminator, EqualTerminator {
  final List<ExecutorWrapper> executorWrappers;

  Terminator(List<ExecutorWrapper> list) {
    this.executorWrappers = list;
  }

  @Override
  public synchronized Terminator addItem(ExecutorService executorService) {
    executorWrappers.add(new ExecutorWrapper(executorService, 0, SECONDS));
    return this;
  }

  @Override
  public synchronized Terminator addItem(ExecutorService executorService, long time, TimeUnit timeUnit) {
    executorWrappers.add(new ExecutorWrapper(executorService, time, timeUnit));
    return this;
  }

  // The following termination logic is partly from:
  // https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html

  @Override
  public synchronized void terminate(long time, TimeUnit timeUnit) {
    List<ExecutorWrapper> executorWrappers = new ArrayList<ExecutorWrapper>(this.executorWrappers);
    for (ExecutorWrapper executorWrapper : executorWrappers) {
      executorWrapper.executorService.shutdown();
    }

    long timeLeft = timeUnit.toNanos(time);
    try {
      for (ExecutorWrapper executorWrapper : executorWrappers) {
        if (timeLeft > 0) {
          long start = System.nanoTime();
          executorWrapper.executorService.awaitTermination(timeLeft, NANOSECONDS);
          timeLeft -= (System.nanoTime() - start);
        }
        executorWrapper.executorService.shutdownNow();
      }

      if (timeLeft > 0) {
        for (ExecutorWrapper executorWrapper : executorWrappers) {
          if (timeLeft <= 0) {
            break;
          }
          long start = System.nanoTime();
          executorWrapper.executorService.awaitTermination(timeLeft, NANOSECONDS);
          timeLeft -= (System.nanoTime() - start);
        }
      }
    }
    catch (InterruptedException ie) {
      // If current thread is interrupted then interrupt all others if was supposed to shutdown properly
      for (ExecutorWrapper executorWrapper : executorWrappers) {
        executorWrapper.executorService.shutdownNow();
      }
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public synchronized void terminate() {
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

    try {
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
    catch (InterruptedException ie) {
      // If current thread is interrupted then interrupt all others if was supposed to shutdown properly
      for (ExecutorWrapper executorWrapper : executorWrappers) {
        executorWrapper.executorService.shutdownNow();
      }
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
