package com.github.taivokasper.executor.service.terminator

import com.winterbe.expekt.should
import java.util.concurrent.ExecutorService

// Helper so that the unit test can access the internal state
fun EqualTerminator.nrOfServices(): Int {
  if (this is Terminator) {
    return this.executorWrappers.size
  }
  throw IllegalStateException("Invalid type")
}

// Helper so that the unit test can access the internal state
fun UnequalTerminator.nrOfServices(): Int {
  if (this is Terminator) {
    return this.executorWrappers.size
  }
  throw IllegalStateException("Invalid type")
}

fun ExecutorService.assertShutdownAndTerminated() {
  this.isShutdown.should.`true`
  this.isTerminated.should.`true`
}
