package com.github.stevegury

import io.Source

object Profiler {

  def reportBreakOut(title: String, graph: Node, println : String => Unit = println) {
    println(title)
    println("-" * title.size)
    val sortedMethods = Analysis.methodsBreakOut(graph).toSeq.sortBy(- _._2)
    // there might be no synthetic root node if there is only a single unique root method to begin with
    val sortedMethodsWithoutRoot = if (sortedMethods(0)._1 == Node.ROOT) sortedMethods.drop(1)
      else sortedMethods
    val (_,total) = sortedMethods(0)
    println("rank\ttime\tname")
    sortedMethodsWithoutRoot.zipWithIndex foreach { case ((name, count), i) =>
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
