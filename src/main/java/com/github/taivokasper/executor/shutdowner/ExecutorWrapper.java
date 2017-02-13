package com.github.taivokasper.executor.shutdowner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

class ExecutorWrapper {
  final ExecutorService executorService;
  final long time;
  final TimeUnit timeUnit;

  ExecutorWrapper(ExecutorService executorService, long time, TimeUnit timeUnit) {
    this.executorService = executorService;
    this.time = time;
    this.timeUnit = timeUnit;
  }
}