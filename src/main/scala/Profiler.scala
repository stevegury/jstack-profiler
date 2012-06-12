package com.github.stevegury

import io.Source

object Profiler {

  def reportBreakOut(title: String, graph: Node) {
    println(title)
    println("-" * title.size)
    val sortedMethods = Analysis.methodsBreakOut(graph).toSeq.sortBy(- _._1)
    val (total,root) = sortedMethods(0)
    require(root == Node.ROOT)
    println("rank\ttime\tname")
    sortedMethods.drop(1).zipWithIndex foreach { case ((count, name), i) =>
      println("%d\t%.02f%%\t%s".format(i, 100 * count.toFloat / total, name))
    }
    println("")
  }

  def main(args: Array[String]) {
    val input = Source.fromInputStream(System.in).getLines
    val graphPerThread = Parser.parse(input)
    val fullGraph = graphPerThread.values reduceLeft { _ + _ }

    reportBreakOut(
      "Runnable methods breakout",
      fullGraph filter { _.state == Runnable }
    )

    reportBreakOut(
      "Blocked/Waiting methods breakout",
      fullGraph filter { node => Seq(Blocked, TimedWaiting, Waiting).contains(node.state) }
    )
  }
}
