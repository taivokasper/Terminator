package com.github.taivokasper.executor.service.terminator.samples;

import java.util.concurrent.Executors;

import org.junit.Test;

import com.github.taivokasper.executor.service.terminator.EqualTerminator;
import com.github.taivokasper.executor.service.terminator.TerminatorFactory;
import com.github.taivokasper.executor.service.terminator.UnequalTerminator;

import static java.util.concurrent.TimeUnit.SECONDS;

public class UsageSampleTest {

  /**
   * Use this method of termination if it is ok to shutdown every executor service at the same time
   * and you want to control the total amount of time spent on shutting down in single place.
   */
  @Test
  public void testTerminatingInEqualTime() throws Exception {
    EqualTerminator equalTerminator = TerminatorFactory.createEqualTerminator();

    equalTerminator.addItem(Executors.newSingleThreadExecutor());
    equalTerminator.addItem(Executors.newCachedThreadPool());
    equalTerminator.addItem(Executors.newFixedThreadPool(3));

    // Now terminate all executor services in total of 5 seconds
    equalTerminator.terminate(5, SECONDS);
  }

  /**
   * Use this method of termination if you need fine grained control over
   * how long shutdown can take for a single executor service.
   */
  @Test
  public void testTerminatingWithDifferentTime() throws Exception {
    UnequalTerminator unequalTerminator = TerminatorFactory.createUnequalTerminator();

    unequalTerminator.addItem(Executors.newSingleThreadExecutor(), 5, SECONDS);
    unequalTerminator.addItem(Executors.newCachedThreadPool(), 15, SECONDS);
    unequalTerminator.addItem(Executors.newFixedThreadPool(1), 1, SECONDS);

    // Now terminate all executor services in total of 15 seconds. The total comes from whatever is the largest timeout.
    // Shutdowns will be performed in ascending timeout order
    unequalTerminator.terminate();
  }
}