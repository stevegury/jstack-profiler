package com.github.stevegury

object Parser {

  val ThreadNameRegex = """"([^"]+)".*""".r
  val CallRegex = """\s+at\s+(.*)""".r
  val StateRegex = """\s+java.lang.Thread.State:\s+(\w+)\s*\(*.*\)*""".r

  def toState(string: String ) = string match {
    case "NEW" => New
    case "RUNNABLE" => Runnable
    case "BLOCKED" => Blocked
    case "WAITING" => Waiting
    case "TIMED_WAITING" => TimedWaiting
    case "TERMINATED" => Terminated
    case _ => throw new IllegalArgumentException("Bad thread state: '" + string + "'")
  }

  def parse(input: Iterator[String]): Map[String, Node] = {
    def recParse(
      threadName: String,
      state: String,
      callStack: List[String],
      result: Map[String, Node]
    ): Map[String, Node] = {
        if(!input.hasNext)
          result
        else {
          val line = input.next()
          line match {
            case ThreadNameRegex(name) => recParse(name, state, callStack, result)
            case CallRegex(call) => recParse(threadName, state, call :: callStack ,result)
            case StateRegex(newState) => recParse(threadName, newState, callStack, result)
            case "" =>
              if(callStack.isEmpty)
                recParse("", "", Nil, result)
              else {
                if (result.contains(threadName)) {
                  val root = result(threadName)
                  val child = root.merge(toGraph(callStack, toState(state)))
                  recParse("", "", Nil, result + (threadName -> child))
                } else
                  recParse("", "", Nil, result + (threadName -> toGraph(callStack, toState(state))))
              }
            case _ => recParse(threadName, state, callStack, result)
          }
        }
    }
    recParse("" , "", Nil, Map.empty[String, Node])
  }

  def toGraph(callStack: List[String], state: State, count: Int = 1): Node = {
    require(callStack != Nil)
    val reversed = callStack.reverse
    val leaf = Node(reversed.head, state, count, Set.empty[Node])
    reversed.tail.foldLeft(leaf){
      case (graph, name) => Node(name, state, count, Set(graph))
    }
  }
}
