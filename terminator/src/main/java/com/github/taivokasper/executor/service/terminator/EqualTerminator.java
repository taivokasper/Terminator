package com.github.taivokasper.executor.service.terminator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public interface EqualTerminator {
  EqualTerminator addItem(ExecutorService executorService);
  void terminate(long time, TimeUnit timeUnit) throws InterruptedException;
}