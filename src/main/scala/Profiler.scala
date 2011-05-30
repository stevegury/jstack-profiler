import java.io.File
import scala.io.Source

object Profiler {

  val DateRegex = """\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d""".r
  val ThreadNameRegex = """"([^"]+)".*""".r
  val CallRegex = """\s+at\s+(.*)""".r
  val StateRegex = """\s+java.lang.Thread.State:\s+(\w+)\s*\(*.*\)*""".r

  abstract class ThreadState
  case object New extends ThreadState
  case object Runnable extends ThreadState
  case object Blocked extends ThreadState
  case object Waiting extends ThreadState
  case object TimedWaiting extends ThreadState
  case object Terminated extends ThreadState

  implicit def string2ThreadState( string : String ) = string match {
    case "NEW" => New
    case "RUNNABLE" => Runnable
    case "BLOCKED" => Blocked
    case "WAITING" => Waiting
    case "TIMED_WAITING" => TimedWaiting
    case "TERMINATED" => Terminated
    case _ => throw new IllegalArgumentException("Bad thread state: '" + string + "'")
  }

  class CallGraph( val name : String = "root" , val state : ThreadState = Runnable, val count : Int = 1, val descendants : Map[String,CallGraph] = Map.empty[String,CallGraph] ) {
    val CallRegex = """([^\(]+)\([^\(]+\)""".r
    lazy val CallRegex(line,file) = name
    lazy val nameSplit = line.split('.')
    lazy val packageName : String = nameSplit.dropRight(2).mkString(".")
    lazy val className : String = nameSplit( nameSplit.size - 2 )
    lazy val methodName : String = nameSplit( nameSplit.size - 1 )
    lazy val fileAndLine = {
      val split = file.split(':')
      if( split.isEmpty )
        None
      else
        Some( split )
    }
    lazy val fileName : Option[String] = fileAndLine.map( _(0) )
    lazy val lineNumber : Option[Int] = fileAndLine.map( _(1).toInt )

    // Update a node of the graph with a call chain (it will create new nodes, or update old ones)
    def update( calls : List[String] , state : ThreadState ) : CallGraph = {
      if( Nil == calls )
        new CallGraph(name, state)
      else{
        val nextCall = descendants.getOrElse( calls.head, new CallGraph(calls.head) )
        val newNextCall = nextCall.update( calls.tail , state )
        new CallGraph(name, Runnable, count + 1, descendants + (newNextCall.name -> newNextCall) )
      }
    }

    // Return the most sampled chain of calls
    def criticalPath : List[String] = innerCriticalPath.tail // remove the "root" node
    private def innerCriticalPath : List[String] = {
      if( descendants.isEmpty )
        List(name)
      else {
        // select the most sampled child and append it to the result
        descendants.values.toSeq.sortWith(_.count > _.count).headOption.map{
          bestChild => name :: bestChild.innerCriticalPath
        }.getOrElse( List(name) )
      }
    }

    // merge two call graphs together, useful for profiling a group of thread (as thread pool)
    def merge( graph : CallGraph ) : CallGraph = {
      if( name == graph.name ) {
        // If nodes are equal (same name), only merge map of descendants
        val (smallest,biggest) = if( count < graph.count )
          (descendants, graph.descendants)
        else
          (graph.descendants, descendants)
        val newDescendants : Map[String,CallGraph] = biggest.map{ case (name,node) =>
          smallest.get(name) match {
            case Some(otherNode) => (name,node.merge(otherNode)) // merge nodes present in both descendant list
            case None => (name,node)
          }
        } ++ (smallest -- biggest.keys) // add node only present in smallest descendant list
        new CallGraph(name, Runnable, count + graph.count, newDescendants )
      }
      else // in case of root inequality, return a new node with the two graphs as children
        new CallGraph(name + "#" + graph.name, Runnable, count + graph.count, Map(name -> this, graph.name -> graph))
    }

    // filter the graph based on the predicate
    def filter( predicate : CallGraph => Boolean ) : CallGraph = {
      // TODO
      this
    }
  }

  def profileThread( it : Iterator[String] , predicate : List[String] => Boolean = _ => true ) : Map[String,CallGraph] = {
    def recProfile( threadName : String , state : String, callStack : List[String], profilingPerThread : Map[String,CallGraph] ) : Map[String,CallGraph] = {
      if( it.hasNext ) {
        val line = it.next()
        line match {
          case ThreadNameRegex(name) => recProfile(name, state, callStack, profilingPerThread)
          case CallRegex(call) => recProfile(threadName, state, call :: callStack ,profilingPerThread)
          case StateRegex(newState) => recProfile(threadName, newState, callStack, profilingPerThread)
          case "" =>
            if( callStack.isEmpty || ! predicate(callStack) )
              recProfile("", "", Nil, profilingPerThread)
            else {
              val root = profilingPerThread.getOrElse(threadName, new CallGraph())
              val callGraph = root.update( callStack , state )
              recProfile("", "", Nil, profilingPerThread + (threadName -> callGraph))
            }
          case _ => recProfile(threadName, state, callStack, profilingPerThread)
        }
      }
      else
        profilingPerThread
    }
    recProfile("" ,"", Nil, Map.empty[String,CallGraph] )
  }

  def sortByUsage( profiling : Map[String,CallGraph] ) : Seq[(String,CallGraph)] = {
    profiling.toSeq.sortWith( _._2.count > _._2.count )
  }

  def main( args : Array[String] ) {
    if( args.isEmpty ) {
      usage()
    }
    else {
      val file = new File( args(0) )
      def filter( word : String )( callstack : List[String] ) = callstack.foldLeft(true){
        case (containsNotString,line) => containsNotString && line.indexOf(word) == -1
      }
      val profilingPerThread = profileThread( Source.fromFile(file).getLines() , filter("YJP") )
      val mergedProfiling = profilingPerThread.values.reduceLeft{ _.merge(_) }
      mergedProfiling.criticalPath.foreach{ x => println(" + "+x) }

      // val runningThreads = profilingPerThread.filter( _.state == Running )
      // val firstThread = sortByUsage(runningThreads).head
      // println( "Critical path of thread: " + firstThread.name )
      // firstThread.criticalPath.foreach{ x => println(" + "+x) }
    }
  }

  private def usage() {
    println("Usage: jstack-profiler <jstack output file>")
  }
}
