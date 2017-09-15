package com.github.taivokasper.executor.service.terminator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public interface UnequalTerminator {
  UnequalTerminator addItem(ExecutorService executorService);
  UnequalTerminator addItem(ExecutorService executorService, long time, TimeUnit timeUnit);
  void terminate() throws InterruptedException;
}