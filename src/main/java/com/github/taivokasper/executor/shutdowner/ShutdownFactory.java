package com.github.taivokasper.executor.shutdowner;

import java.util.ArrayList;

public class ShutdownFactory {
  public static MultiTimeoutExecutorServiceContainer createMultiTimeoutContainer() {
    return new Shutdowner(new ArrayList<ExecutorWrapper>());
  }

  public static SingleTimeoutExecutorServiceContainer createSingleTimeoutContainer() {
    return new Shutdowner(new ArrayList<ExecutorWrapper>());
  }
}