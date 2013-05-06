package com.github.stevegury

object Analysis {
  def criticalPath(graph: Node, path: List[String] = Nil): Node = {
    if (graph.children.isEmpty) {
      val names = (graph.name :: path).reverse
      Parser.toGraph(names, graph.state, graph.count)
    }
    else {
      val next = graph.children.toSeq.sortBy(_.count).last
      criticalPath(next, graph.name :: path)
    }
  }

  def topCriticalPath(graph: Node, n: Int, res: Option[Node] = None): Node =
    if (n == 0)
      res.get
    else {
      val path = criticalPath(graph)
      val newRes = res map { _ + path } orElse Some(path)
      topCriticalPath(graph - path, n - 1, newRes)
    }

  def methodsBreakOut(graph: Node): Map[String, Int] = {
    def loop(graph: Node): Map[String, Int] = {
      Map(graph.name -> graph.count) ++ (graph.children flatMap { loop(_) })
    }
    loop(graph)
  }
}
