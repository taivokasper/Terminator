package com.github.taivokasper.executor.service.terminator

import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

object TerminatorFixture {
  fun idleExecutor(): ExecutorService = Executors.newSingleThreadExecutor()

  private val whileTrueSleep: () -> TimestampedException = work@ {
    while (true) {
      try {
        Thread.sleep(50)
      } catch (e: InterruptedException) {
        return@work TimestampedException(e, System.nanoTime())
      }
    }
    // This cannot happen but the Kotlin compiler is not smart enough to know that
    @Suppress("UNREACHABLE_CODE")
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
