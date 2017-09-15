package com.github.taivokasper.executor.service.terminator

import com.winterbe.expekt.should
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.lifecycle.CachingMode
import org.jetbrains.spek.subject.SubjectSpek
import java.util.concurrent.TimeUnit

class EqualTerminatorSpec : SubjectSpek<EqualTerminator>({
  subject(CachingMode.TEST) { TerminatorFactory.createEqualTerminator() }

  context("Idle executor services") {
    val services = arrayOf(
        TerminatorFixture.idleExecutor(),
        TerminatorFixture.idleExecutor(),
        TerminatorFixture.idleExecutor()
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

  context("Working executors are interrupted") {
    val workingExecutors = arrayOf(
        TerminatorFixture.workingInterruptableExecutor(),
        TerminatorFixture.workingInterruptableExecutor(),
        TerminatorFixture.workingInterruptableExecutor()
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
