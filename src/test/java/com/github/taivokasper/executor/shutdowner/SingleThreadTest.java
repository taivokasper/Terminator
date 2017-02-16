package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import static com.github.taivokasper.executor.shutdowner.WorkHelpers.whileTrueSleep;

public class SingleThreadTest {
  @Test
  public void testSingleShutdownImmediate() throws Exception {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ShutdownFactory.createBlocking()
        .addShutdownItem(executorService)
        .terminate();
    Assert.assertTrue(executorService.isShutdown());
    Assert.assertTrue(executorService.isTerminated());
  }

  @Test(expected = InterruptedException.class)
  public void testSingleShutdownImmediateInterruption() throws Exception {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    CompletableFuture<Exception> future = CompletableFuture.supplyAsync(whileTrueSleep, executorService);

    ShutdownFactory.createBlocking()
        .addShutdownItem(executorService)
        .terminate();

    Assert.assertTrue(executorService.isShutdown());
    // There is no time to fully terminate
    Assert.assertFalse(executorService.isTerminated());
    throw future.get();
  }
}