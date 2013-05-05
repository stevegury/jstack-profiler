import com.github.stevegury._
import org.scalatest.{FunSpec, GivenWhenThen}

class ReportBreakOutSpec extends FunSpec with GivenWhenThen {
  describe("Report Break Out") {
    it("should allow thread dumps containing a single thread") {
      Given("a thread dump containing a single thread ")
      val graphPerThread = Parser.parse(
        """
          |2013-04-10 14:37:34
          |Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.2-b06 mixed mode):
          |
          |"Thread-255" prio=6 tid=0x00000000067a2800 nid=0x374 runnable [0x000000001bede000]
          |   java.lang.Thread.State: RUNNABLE
          |	at p.C.m3(C.java:74)
          |	at java.lang.Thread.run(Unknown Source)
          |
          |2013-04-10 14:37:35
          |Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.2-b06 mixed mode):
          |
          |"Thread-255" prio=6 tid=0x00000000067a2800 nid=0x374 runnable [0x000000001bede000]
          |   java.lang.Thread.State: RUNNABLE
          |	at p.B.m3(C.java:73)
          |	at java.lang.Thread.run(Unknown Source)
          |
        """.stripMargin.split("\n").iterator)
      val fullGraph = graphPerThread.values reduceLeft {
        _ + _
      }
      When("a report break out is run")
      Profiler.reportBreakOut(
        "Runnable methods breakout",
        fullGraph filter {
          _.state == Runnable
        }
      )
      Then("no exception should be thrown")
    }

    it("should correctly report methods that are called the same number of times") {
      Given("a thread dump containing an equal number of appearances of two methods")
      val graphPerThread = Parser.parse(
        """
          |2013-04-10 14:37:34
          |Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.2-b06 mixed mode):
          |
          |"Thread-255" prio=6 tid=0x00000000067a2800 nid=0x374 runnable [0x000000001bede000]
          |   java.lang.Thread.State: RUNNABLE
          |	at p.C.method2(C.java:74)
          |	at java.lang.Thread.run(Unknown Source)
          |
          |2013-04-10 14:37:35
          |Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.2-b06 mixed mode):
          |
          |"Thread-255" prio=6 tid=0x00000000067a2800 nid=0x374 runnable [0x000000001bede000]
          |   java.lang.Thread.State: RUNNABLE
          |	at p.B.method3(C.java:73)
          |	at java.lang.Thread.run(Unknown Source)
          |
        """.stripMargin.split("\n").iterator)
      val fullGraph = graphPerThread.values reduceLeft {
        _ + _
      }
      When("a report break out is run")
      val report = new StringBuilder
      Profiler.reportBreakOut(
        "Runnable methods breakout",
        fullGraph filter {
          _.state == Runnable
        },
        s => report.append(s)
      )
      Then("the first method should appear in the report")
      assert(report.toString.contains("method3"), "Report did not contain 'method3': " + report)
      And("the second method should appear in the report")
      assert(report.toString.contains("method2"), "Report did not contain 'method2': " + report)
    }
  }
}
