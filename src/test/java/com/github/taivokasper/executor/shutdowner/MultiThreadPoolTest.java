package com.github.taivokasper.executor.shutdowner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MultiThreadPoolTest {

  @Test
  public void testShutdownImmediate() throws Exception {
    ExecutorService executorService1 = Executors.newSingleThreadExecutor();
    ExecutorService executorService2 = Executors.newSingleThreadExecutor();
    ExecutorService executorService3 = Executors.newSingleThreadExecutor();

    ShutdownFactory.createBlocking()
        .addShutdownItem(executorService1)
        .addShutdownItem(executorService2)
        .addShutdownItem(executorService3)
        .terminate();

    Assert.assertTrue(executorService1.isShutdown());
    Assert.assertTrue(executorService1.isTerminated());

    Assert.assertTrue(executorService2.isShutdown());
    Assert.assertTrue(executorService2.isTerminated());

    Assert.assertTrue(executorService3.isShutdown());
    Assert.assertTrue(executorService3.isTerminated());
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

    ShutdownFactory.createBlocking()
        .addShutdownItem(executorService1, 1, SECONDS)
        .addShutdownItem(executorService2, 3, SECONDS)
        .addShutdownItem(executorService3, 2, SECONDS)
        .addShutdownItem(executorService4)
        .terminate();

    Long[] futureEndTimes = { future1.get(), future2.get(), future3.get(), future4.get() };
    Arrays.sort(futureEndTimes);

    // Assert that the futures are finished in the correct order
    Assert.assertEquals(future1.get(), futureEndTimes[1]);
    Assert.assertEquals(future2.get(), futureEndTimes[3]);
    Assert.assertEquals(future3.get(), futureEndTimes[2]);
    Assert.assertEquals(future4.get(), futureEndTimes[0]);

    Assert.assertTrue(executorService1.isShutdown());
    Assert.assertTrue(executorService1.isTerminated());

    Assert.assertTrue(executorService2.isShutdown());
    Assert.assertTrue(executorService2.isTerminated());

    Assert.assertTrue(executorService3.isShutdown());
    Assert.assertTrue(executorService3.isTerminated());

    Assert.assertTrue(executorService4.isShutdown());
    Assert.assertTrue(executorService4.isTerminated());
  }
}