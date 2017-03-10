package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SingleTimeoutExecutorServiceContainerTest {
  private SingleTimeoutExecutorServiceContainer singleTimeoutContainer;

  @Before
  public void init() throws Exception {
    singleTimeoutContainer = ShutdownFactory.createSingleTimeoutContainer();
  }

  @Test
  public void shutdownMultipleIdleExecutors() throws Exception {
    ExecutorService[] executors = new ExecutorService[10];
    for (int i = 0; i < executors.length; i++) {
      executors[i] = Executors.newSingleThreadExecutor();
    }

    for (int i = 0; i < executors.length; i++) {
      singleTimeoutContainer.addShutdownItem(executors[i]);
    }

    singleTimeoutContainer.terminate(10, MILLISECONDS);

    for (int i = 0; i < executors.length; i++) {
      Assert.assertTrue(executors[i].isShutdown());
      Assert.assertTrue(executors[i].isTerminated());
    }
  }

  @Test
  public void shutdownMultipleWorkingExecutors() throws Exception {
    ExecutorService[] executors = new ExecutorService[10];
    CompletableFuture<Exception>[] exceptions = new CompletableFuture[executors.length];
    for (int i = 0; i < executors.length; i++) {
      executors[i] = Executors.newSingleThreadExecutor();
      exceptions[i] = CompletableFuture.supplyAsync(WorkHelpers.whileTrueSleep, executors[i]);
    }

    for (int i = 0; i < executors.length; i++) {
      singleTimeoutContainer.addShutdownItem(executors[i]);
    }

    singleTimeoutContainer.terminate(10, MILLISECONDS);

    for (int i = 0; i < executors.length; i++) {
      Assert.assertTrue(exceptions[i].get() instanceof InterruptedException);
      Assert.assertTrue(executors[i].isShutdown());
      Assert.assertTrue(executors[i].isTerminated());
    }
  }
}