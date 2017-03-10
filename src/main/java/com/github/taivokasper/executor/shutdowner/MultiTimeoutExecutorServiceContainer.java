package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public interface MultiTimeoutExecutorServiceContainer {
  MultiTimeoutExecutorServiceContainer addShutdownItem(ExecutorService executorService);
  MultiTimeoutExecutorServiceContainer addShutdownItem(ExecutorService executorService, long time, TimeUnit timeUnit);
  void terminate() throws InterruptedException;
}