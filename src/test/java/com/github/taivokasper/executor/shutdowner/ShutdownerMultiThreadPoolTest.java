package com.github.taivokasper.executor.shutdowner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ShutdownerMultiThreadPoolTest {
  @Test
  public void testShutdownImmediate() throws Exception {
    ExecutorService executorService1 = Executors.newSingleThreadExecutor();
    ExecutorService executorService2 = Executors.newSingleThreadExecutor();
    ExecutorService executorService3 = Executors.newSingleThreadExecutor();

    Shutdowner.create()
        .addShutdownItem(executorService1)
        .addShutdownItem(executorService2)
        .addShutdownItem(executorService3)
        .startShutdown();

    AssertHelpers.assertShutdownTerminated(executorService1);
    AssertHelpers.assertShutdownTerminated(executorService2);
    AssertHelpers.assertShutdownTerminated(executorService3);
  }

  @Test
  public void testShutdownTimeout() throws Exception {
    ExecutorService executorService1 = Executors.newSingleThreadExecutor();
    ExecutorService executorService2 = Executors.newSingleThreadExecutor();
    ExecutorService executorService3 = Executors.newSingleThreadExecutor();
    ExecutorService executorService4 = Executors.newSingleThreadExecutor();

    CompletableFuture<Long> future1 = CompletableFuture.supplyAsync(WorkHelpers.runtimeMeasurer, executorService1);
    CompletableFuture<Long> future2 = CompletableFuture.supplyAsync(WorkHelpers.runtimeMeasurer, executorService2);
    CompletableFuture<Long> future3 = CompletableFuture.supplyAsync(WorkHelpers.runtimeMeasurer, executorService3);
    CompletableFuture<Long> future4 = CompletableFuture.supplyAsync(WorkHelpers.runtimeMeasurer, executorService4);

    Shutdowner.create()
        .addShutdownItem(executorService1, 1, SECONDS)
        .addShutdownItem(executorService2, 3, SECONDS)
        .addShutdownItem(executorService3, 2, SECONDS)
        .addShutdownItem(executorService4)
        .startShutdown();

    Long[] futureEndTimes = { future1.get(), future2.get(), future3.get(), future4.get() };
    Arrays.sort(futureEndTimes);

    // Assert that the futures are finished in the correct order
    Assert.assertEquals(future1.get(), futureEndTimes[1]);
    Assert.assertEquals(future2.get(), futureEndTimes[3]);
    Assert.assertEquals(future3.get(), futureEndTimes[2]);
    Assert.assertEquals(future4.get(), futureEndTimes[0]);

    AssertHelpers.assertShutdownTerminated(executorService1);
    AssertHelpers.assertShutdownTerminated(executorService2);
    AssertHelpers.assertShutdownTerminated(executorService3);
    AssertHelpers.assertShutdownTerminated(executorService4);
  }
}