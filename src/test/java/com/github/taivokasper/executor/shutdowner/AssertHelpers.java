package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.ExecutorService;

import org.junit.Assert;

public class AssertHelpers {
  static void assertShutdownTerminated(ExecutorService executorService) {
    Assert.assertTrue(executorService.isShutdown());
    Assert.assertTrue(executorService.isTerminated());
  }

  static long increaseByPercentage(long value, long percentage) {
    return value + (value * percentage / 100);
  }
}