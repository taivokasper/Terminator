package com.github.taivokasper.executor.service.terminator

import com.winterbe.expekt.should
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.lifecycle.CachingMode
import org.jetbrains.spek.subject.SubjectSpek
import java.util.concurrent.TimeUnit

class UnequalTerminatorSpec : SubjectSpek<UnequalTerminator>({
  subject(CachingMode.TEST) { TerminatorFactory.createUnequalTerminator() }

  context("Idle executor services") {
    val services = arrayOf(
        TerminatorFixture.workingInterruptableExecutor(),
        TerminatorFixture.workingInterruptableExecutor(),
        TerminatorFixture.workingInterruptableExecutor()
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

  context("Working executors are interrupted") {
    val workingExecutors = arrayOf(
        TerminatorFixture.workingInterruptableExecutor(),
        TerminatorFixture.workingInterruptableExecutor(),
        TerminatorFixture.workingInterruptableExecutor()
    )
    beforeEachTest {
      subject.addItem(workingExecutors[1].exec, 50, TimeUnit.MILLISECONDS)
      subject.addItem(workingExecutors[0].exec)
      subject.addItem(workingExecutors[2].exec, 100, TimeUnit.MILLISECONDS)
    }

    context("Terminate ongoing work") {
      beforeEachTest {
        subject.terminate()
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
        // Set interrupt flag to main running thread
        Thread.currentThread().interrupt()
        subject.terminate()
      }

      afterEachTest {
        // Hack to unset the interrupt so that the thread running tests wouldn't stop
        try {
          Thread.sleep(1)
          throw IllegalStateException("Interrupt was not thrown!")
        } catch (ie: InterruptedException) {

        }
      }

      it("Interrupts workers") {
        workingExecutors.map { it.timestampedException.get(1, TimeUnit.SECONDS).exception }.forEach {
          it.should.instanceof(InterruptedException::class.java)
        }
      }

      it("Shuts down and terminates all workers") {
        workingExecutors.map { it.exec }.forEach {
          it.assertShutdownAndTerminated()
        }
      }
    }
  }
})
