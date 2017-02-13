package com.github.taivokasper.executor.shutdowner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Shutdowner {
  private final Set<ExecutorWrapper> executorWrappers;

  private Shutdowner() {
    executorWrappers = new TreeSet<ExecutorWrapper>(new Comparator<ExecutorWrapper>() {
      @Override
      public int compare(ExecutorWrapper e1, ExecutorWrapper e2) {
        return Long.valueOf(e1.timeUnit.toNanos(e1.time)).compareTo(e2.timeUnit.toNanos(e2.time));
      }
    });
  }

  public static Shutdowner create() {
    return new Shutdowner();
  }

  public Shutdowner addShutdownItem(ExecutorService executorService) {
    executorWrappers.add(new ExecutorWrapper(executorService, 0, SECONDS));
    return this;
  }

  public Shutdowner addShutdownItem(ExecutorService executorService, long time, TimeUnit timeUnit) {
    executorWrappers.add(new ExecutorWrapper(executorService, time, timeUnit));
    return this;
  }

  // TODO check from https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html on how to properly shutdown
  public void startShutdown() throws InterruptedException {
    List<ExecutorWrapper> executorWrappers = new ArrayList<ExecutorWrapper>(this.executorWrappers);

    long startTime = System.nanoTime();
    ExecutorWrapper longestTimeout = executorWrappers.get(executorWrappers.size() - 1);
    long endTime = startTime + longestTimeout.timeUnit.toNanos(longestTimeout.time);

    for (ExecutorWrapper executorWrapper : executorWrappers) {
      executorWrapper.executorService.shutdown();
    }

    long timeLeft = endTime - startTime;
    for (ExecutorWrapper executorWrapper : executorWrappers) {
      if (timeLeft > 0) {
        long minAwait = Math.min(timeLeft, executorWrapper.timeUnit.toNanos(executorWrapper.time));
        executorWrapper.executorService.awaitTermination(minAwait, NANOSECONDS);
      }
      executorWrapper.executorService.shutdownNow();

      timeLeft -= executorWrapper.timeUnit.toNanos(executorWrapper.time);
    }
  }
}