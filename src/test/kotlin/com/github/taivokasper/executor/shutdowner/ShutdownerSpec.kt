package com.github.taivokasper.executor.shutdowner

import com.winterbe.expekt.should
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.subject.SubjectSpek
import java.lang.Exception
import java.util.concurrent.*

class SingleTimeoutSpec : SubjectSpek<SingleTimeoutExecutorServiceContainer>({
  subject { ShutdownFactory.createSingleTimeoutContainer() }

  context("Idle executor services") {
    val services = arrayOf(
        idleExecutor(),
        idleExecutor(),
        idleExecutor()
    )
    beforeEachTest {
      services.forEach {
        subject.addShutdownItem(it)
      }
    }

    it("Should add all services") {
      services.size.should.equal(subject.nrOfServices())
    }

    it("Should properly shutdown all services") {
      subject.terminate(1, TimeUnit.MILLISECONDS)
      services.forEach {
        it.assertShutdownAndTerminated()
      }
    }
  }

  context("Working executors interrupted") {
    val workingExecutors = arrayOf(
        workingInterruptableExecutor(),
        workingInterruptableExecutor(),
        workingInterruptableExecutor()
    )
    beforeEachTest {
      workingExecutors.forEach {
        subject.addShutdownItem(it.exec)
      }
    }

    context("Terminate ongoing work") {
      beforeEachTest {
        subject.terminate(50, TimeUnit.MILLISECONDS)
      }

      it("Interrupts all work") {
        workingExecutors.map { it.timestampedException.get(1, TimeUnit.SECONDS).exception }.forEach {
          it.should.instanceof(InterruptedException::class.java)
        }
      }

      it("Should properly shutdown all services") {
        workingExecutors.map { it.exec }.forEach {
          it.assertShutdownAndTerminated()
        }
      }
    }

    context("Main execution thread interrupted while shutting down") {
      beforeEachTest {
        Thread.currentThread().interrupt()
        subject.terminate(5, TimeUnit.MILLISECONDS)
      }

      // Unset the set interrupt
      afterEachTest {
        try {
          Thread.sleep(1)
        }
        catch (ie: InterruptedException) {

        }
      }

      it("Interrupts workers") {
        workingExecutors.map { it.timestampedException.get(1, TimeUnit.SECONDS).exception }.forEach {
          it.should.instanceof(InterruptedException::class.java)
        }
      }

      it("Shuts down and terminates") {
        workingExecutors.map { it.exec }.forEach {
          it.assertShutdownAndTerminated()
        }
      }
    }
  }
})

class MultiTimeoutSpec : SubjectSpek<MultiTimeoutExecutorServiceContainer>({
  subject { ShutdownFactory.createMultiTimeoutContainer() }

  context("Idle executor services") {
    val services = arrayOf(
        workingInterruptableExecutor(),
        workingInterruptableExecutor(),
        workingInterruptableExecutor()
    )
    beforeEachTest {
      subject.addShutdownItem(services[1].exec, 50, TimeUnit.MILLISECONDS)
      subject.addShutdownItem(services[0].exec)
      subject.addShutdownItem(services[2].exec, 100, TimeUnit.MILLISECONDS)
    }

    it("Should add all services") {
      services.size.should.equal(subject.nrOfServices())
    }

    it("Shuts down in descending timeout order") {
      subject.terminate()
      val expectedShutdownOrder = arrayListOf(services[2], services[1], services[0])
      val actualShutdownOrder = services.sortedByDescending { it.timestampedException.get(1, TimeUnit.SECONDS).time }

      expectedShutdownOrder.should.equal(actualShutdownOrder)
    }
  }
})

fun SingleTimeoutExecutorServiceContainer.nrOfServices(): Int {
  if (this is Shutdowner) {
    return this.executorWrappers.size
  }
  throw IllegalStateException("Invalid type")
}

fun MultiTimeoutExecutorServiceContainer.nrOfServices(): Int {
  if (this is Shutdowner) {
    return this.executorWrappers.size
  }
  throw IllegalStateException("Invalid type")
}

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

fun ExecutorService.assertShutdownAndTerminated() {
  this.isShutdown.should.`true`
  this.isTerminated.should.`true`
}

data class ExecutorWork(val timestampedException: Future<TimestampedException>, val exec: ExecutorService)
data class TimestampedException(val exception: Exception, val time: Long)