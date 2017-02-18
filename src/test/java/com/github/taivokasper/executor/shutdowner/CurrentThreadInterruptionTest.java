package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

public class CurrentThreadInterruptionTest {
  @Test(expected = InterruptedException.class)
  public void testSingleShutdownImmediate() throws Exception {
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    CompletableFuture<Exception> worker = CompletableFuture.supplyAsync(WorkHelpers.whileTrueSleep, executorService);

    Thread.currentThread().interrupt();

    ShutdownFactory.createBlocking()
        .addShutdownItem(executorService)
        .terminate();

    Assert.assertTrue(executorService.isShutdown());
    // No time to fully terminate
    Assert.assertFalse(executorService.isTerminated());

    throw worker.get();
  }
}