package com.github.taivokasper.executor.service.terminator

import com.winterbe.expekt.should
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.subject.SubjectSpek
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class SingleTimeoutSpec : SubjectSpek<EqualTerminator>({
  subject { TerminatorFactory.createEqualTerminator() }

  context("Idle executor services") {
    val services = arrayOf(
        ShutdownerFixture.idleExecutor(),
        ShutdownerFixture.idleExecutor(),
        ShutdownerFixture.idleExecutor()
    )
    beforeEachTest {
      services.forEach {
        subject.addItem(it)
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
        ShutdownerFixture.workingInterruptableExecutor(),
        ShutdownerFixture.workingInterruptableExecutor(),
        ShutdownerFixture.workingInterruptableExecutor()
    )
    beforeEachTest {
      workingExecutors.forEach {
        subject.addItem(it.exec)
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

      // Unset the set interrupt so that the thread running tests wouldn't stop
      afterEachTest {
        try {
          Thread.sleep(1)
          throw IllegalStateException("Interrupt was not thrown!")
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

class MultiTimeoutSpec : SubjectSpek<UnequalTerminator>({
  subject { TerminatorFactory.createUnequalTerminator() }

  context("Idle executor services") {
    val services = arrayOf(
        ShutdownerFixture.workingInterruptableExecutor(),
        ShutdownerFixture.workingInterruptableExecutor(),
        ShutdownerFixture.workingInterruptableExecutor()
    )
    beforeEachTest {
      subject.addItem(services[1].exec, 50, TimeUnit.MILLISECONDS)
      subject.addItem(services[0].exec)
      subject.addItem(services[2].exec, 100, TimeUnit.MILLISECONDS)
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
