package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public interface ShutdownHelper {
  ShutdownHelper addShutdownItem(ExecutorService executorService);
  ShutdownHelper addShutdownItem(ExecutorService executorService, long time, TimeUnit timeUnit);
  void terminate() throws InterruptedException;
}