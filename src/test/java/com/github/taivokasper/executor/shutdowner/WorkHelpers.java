package com.github.taivokasper.executor.shutdowner;

import java.util.function.Supplier;

class WorkHelpers {
  static final Supplier<Exception> whileTrueSleep = () -> {
    while (true) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
        return e;
      }
    }
  };

  static final Supplier<Long> runtimeMeasurer = () -> {
    while (true) {
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
        return System.nanoTime();
      }
    }
  };
}