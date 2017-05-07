package com.github.taivokasper.executor.shutdowner

import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

object ShutdownerFixture {
  fun idleExecutor(): ExecutorService {
    return Executors.newSingleThreadExecutor()
  }

  val whileTrueSleep: () -> TimestampedException = work@ {
    while (true) {
      try {
        Thread.sleep(50)
      } catch (e: InterruptedException) {
        return@work TimestampedException(e, System.nanoTime())
      }
    }
    // This cannot happen but the compiler is not smart enough
    return@work TimestampedException(IllegalStateException("Something went wrong"), System.nanoTime())
  }

  fun workingInterruptableExecutor(): ExecutorWork {
    val exec = Executors.newSingleThreadExecutor()
    val future = FutureTask(whileTrueSleep)
    exec.execute(future)
    return ExecutorWork(future, exec)
  }

  data class ExecutorWork(val timestampedException: Future<TimestampedException>, val exec: ExecutorService)
  data class TimestampedException(val exception: Exception, val time: Long)
}