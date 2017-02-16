package com.github.taivokasper.executor.shutdowner;

public class ShutdownFactory {
  public static ShutdownHelper createBlocking() {
    return new BlockingShutdownHelper();
  }
}