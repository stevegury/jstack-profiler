package com.github.stevegury

abstract class State
case object New extends State
case object Runnable extends State
case object Blocked extends State
case object Waiting extends State
case object TimedWaiting extends State
case object Terminated extends State

object Node {
  val ROOT = "root"

  def filterRoot(names: List[String]) =
    if (names.head == Node.ROOT)
      names.tail
    else
      names
}

case class Node(name: String, state: State, count: Int, children: Set[Node] = Set.empty[Node]) {

  def expand(current: List[String] = Nil): List[Node] =
    if (children.isEmpty) {
      val names = Node.filterRoot((name :: current).reverse)
      def node  = Parser.toGraph(names, state)
      (1 to count) map { _ => node } toList
    } else
      children.toList flatMap { _.expand(name :: current) }

  def merge(graph: Node): Node =
    if (this == graph)
      Node(name, state, count + graph.count, mergeChildren(children, graph.children))
    else if (name == Node.ROOT)
      Node(Node.ROOT, state, count + graph.count, mergeChildren(children, Set(graph)))
    else if (graph.name == Node.ROOT)
      Node(Node.ROOT, graph.state, count + graph.count, mergeChildren(Set(this), graph.children))
    else
      Node(Node.ROOT, Runnable, count + graph.count, Set(this, graph))

  def +(graph: Node) = merge(graph)

  def substract(graph: Node): Node =
    if (this != graph)
      this
    else {
      Node(name, state, count - graph.count, substractChildren(children, graph.children))
    }

  def -(graph: Node) = substract(graph)

  def filter(predicate: Node => Boolean): Node =
    expand() filter { predicate(_) } reduceLeft { _.merge(_) }

  private[this] def mergeChildren(
    lefts: Set[Node],
    rights: Set[Node],
    merged: Set[Node] = Set.empty[Node]
  ): Set[Node] = {
    if (lefts.isEmpty)
      merged ++ rights
    else {
      val node = lefts.head
      if (rights.contains(node))
        mergeChildren(lefts - node, rights - node, merged + node.merge(rights.find(_ == node).get))
      else
        mergeChildren(lefts - node, rights, merged + node)
    }
  }

  private[this] def substractChildren(
    lefts: Set[Node],
    rights: Set[Node],
    res: Set[Node] = Set.empty[Node]
  ): Set[Node] = {
    if (rights.isEmpty)
      lefts ++ res
    else {
      val node = rights.head
      if (lefts.contains(node)) {
        val left = lefts.find(_ == node).get
        val substracted = left.substract(node)
        if (substracted.count <= 0)
          substractChildren(lefts - node, rights - node, res)
        else
          substractChildren(lefts - node, rights - node, res + left.substract(node))
      } else {
        substractChildren(lefts, rights - node, res)
      }
    }
  }

  override def toString() = nodeString(this, count) + "\n" + display(this, count)

  private[this] def display(node: Node, total: Int, prefix: String = ""): String = {
    var output = ""
    val children = node.children.toSeq.sortBy(_.count).reverse
    if (!children.isEmpty) {
      children.dropRight(1) foreach { n =>
        output += "%s +-- %s\n".format(prefix, nodeString(n, total))
        output += display(n, total, prefix + " |  ")
      }
      val last = children.last
      output += "%s +-- %s\n".format(prefix, nodeString(last, total))
      output += display(last, total, prefix + "    ")
    }
    output
  }

  private[this] def nodeString(node: Node, total: Int) = {
    "%s [%.02f%%]".format(node.name, 100 * node.count.toFloat / total)
  }

  override def hashCode() = (name + state).hashCode
  override def equals(other: Any) = other match {
    case node: Node => (name + state).hashCode == node.hashCode
    case _ => false
  }
}
