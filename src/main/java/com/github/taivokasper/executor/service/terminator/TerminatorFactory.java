package com.github.taivokasper.executor.service.terminator;

import java.util.ArrayList;

public class TerminatorFactory {
  public static EqualTerminator createEqualTerminator() {
    return new Terminator(new ArrayList<ExecutorWrapper>());
  }

  public static UnequalTerminator createUnequalTerminator() {
    return new Terminator(new ArrayList<ExecutorWrapper>());
  }
}
