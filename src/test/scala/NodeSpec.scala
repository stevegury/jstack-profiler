import com.github.stevegury.{Blocked, Node, Parser, Runnable}
import io.Source
import java.net.URI
import org.scalatest.{FunSpec, GivenWhenThen}

/**
 *
 */
class NodeSpec extends FunSpec with GivenWhenThen {

  describe("NodeHolder") {
    it("should indicate that two childless nodes are equal if the have the same data in the nodes") {
      assert(new NodeHolder(new Node("a", Blocked, 1, Set())) === new NodeHolder(new Node("a", Blocked, 1, Set())))
    }
    it("should indicate that two childless nodes are NOT equal if the have the different data in the nodes") {
      assert(!(new NodeHolder(new Node("a", Blocked, 1, Set())) equals new NodeHolder(new Node("a", Blocked, 2, Set()))))
    }
    it("should indicate that two nodes are equal if they have the same data and same children") {
      assert(new NodeHolder(new Node("a", Blocked, 1, Set(new Node("b", Blocked, 1, Set())))) === new NodeHolder(new Node("a", Blocked, 1, Set(new Node("b", Blocked, 1, Set())))))
    }
    it("should indicate that two nodes are NOT equal if they have the same data but the data of their children differ") {
      assert(!(
        new NodeHolder(new Node("a", Blocked, 1, Set(new Node("b", Blocked, 1, Set()))))
          equals
        new NodeHolder(new Node("a", Blocked, 1, Set(new Node("b", Blocked, 2, Set()))))
      ))
    }
    it("should indicate that two nodes are NOT equal if they have the same data but one has a child that the other does not") {
      assert(!(
        new NodeHolder(new Node("a", Blocked, 1, Set(new Node("b", Blocked, 1, Set()), new Node("c", Blocked, 1, Set()))))
          equals
        new NodeHolder(new Node("a", Blocked, 1, Set(new Node("b", Blocked, 1, Set()))))
      ))
    }
  }

  describe("Node") {
    it("should be equal after filtering") {
      Given("a single thread graph where one of the stacktraces is a subset of another")
      val graphPerThread = Parser.parse(
        """
          |2013-04-10 14:37:34
          |Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.2-b06 mixed mode):
          |
          |"Thread-255" prio=6 tid=0x00000000067a2800 nid=0x374 runnable [0x000000001bede000]
          |   java.lang.Thread.State: RUNNABLE
          |	at p.C.m4(C.java:74)
          |	at java.lang.Thread.run(Unknown Source)
          |
          |2013-04-10 14:37:35
          |Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.2-b06 mixed mode):
          |
          |"Thread-255" prio=6 tid=0x00000000067a2800 nid=0x374 runnable [0x000000001bede000]
          |   java.lang.Thread.State: RUNNABLE
          |	at p.D.m6(C.java:76)
          |	at p.B.m3(C.java:73)
          |	at java.lang.Thread.run(Unknown Source)
          |
          |2013-04-10 14:37:36
          |Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.2-b06 mixed mode):
          |
          |"Thread-255" prio=6 tid=0x00000000067a2800 nid=0x374 runnable [0x000000001bede000]
          |   java.lang.Thread.State: RUNNABLE
          |	at p.B.m3(C.java:73)
          |	at java.lang.Thread.run(Unknown Source)
          |
        """.stripMargin.split("\n").iterator)
      val singleThreadGraph = graphPerThread.values.iterator.next()
      When("the node is filtered with an always true filter")
      val filtered = singleThreadGraph filter { _ => true }
      Then("the unfiltered Node should be equal to the filtered Node")
      assert(new NodeHolder(singleThreadGraph) === new NodeHolder(filtered))
    }
  }
}

/*
 * Wrap a node and override equals to allow for deep equality testing.
 * @param node
 */
private class NodeHolder(val node : Node) {
  override def equals(that : Any) = that match {
    case thatNode : NodeHolder =>
      node.name.equals(thatNode.node.name) &&
        node.state.equals(thatNode.node.state) &&
        node.count.equals(thatNode.node.count) &&
        node.children.map(new NodeHolder(_)).equals(thatNode.node.children.map(new NodeHolder(_)))
    case _ => false
  }

  override def hashCode = node.hashCode() + node.children.hashCode()

  override def toString = node.toString()
}
