package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public interface SingleTimeoutExecutorServiceContainer {
  SingleTimeoutExecutorServiceContainer addShutdownItem(ExecutorService executorService);
  void terminate(long time, TimeUnit timeUnit) throws InterruptedException;
}