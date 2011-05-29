import java.io.File
import scala.io.Source

object Profiler {

  val DateRegex = """\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d""".r
  val ThreadNameRegex = """"([^"]+)".*""".r
  val CallRegex = """\tat (.*)""".r
  val StateRegex = """   java.lang.Thread.State: (\w+)\s*\(*.*\)*""".r

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

  class CallGraph( val name : String , val state : ThreadState = Runnable, val count : Int = 1, val descendants : Map[String,CallGraph] = Map.empty[String,CallGraph] ) {
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
        val nextCall : CallGraph = descendants.get( calls.head ) match {
          case Some(nextCall) => nextCall
          case _ => new CallGraph(calls.head)
        }
        val newNextCall = nextCall.update( calls.tail , state )
        new CallGraph(name, Runnable, count + 1, descendants + (newNextCall.name -> newNextCall) )
      }
    }

    // Return the most sampled chain of calls
    def criticalPath : List[String] = {
      if( descendants.isEmpty )
        List(name)
      else {
        // select the most sampled child and append it to the result
        descendants.values.toSeq.sortWith(_.count > _.count).headOption.map{
          bestChild => name :: bestChild.criticalPath
        }.getOrElse( List(name) )
      }
    }

    // merge two call graphs together, useful for profiling a group of thread (as thread pool)
    def merge( graph : CallGraph ) : CallGraph = {
      this
    }

    // filter the graph based on the predicate
    def filter( predicate : CallGraph => Boolean ) : CallGraph = {
      // TODO
      this
    }
  }

  def profileThread( it : Iterator[String] ) : Map[String,CallGraph] = {
    def recProfile( threadName : String , state : String, callStack : List[String], profilingPerThread : Map[String,CallGraph] ) : Map[String,CallGraph] = {
      if( it.hasNext ) {
        val line = it.next()
        line match {
          case ThreadNameRegex(name) => recProfile(name, state, callStack, profilingPerThread)
          case CallRegex(call) => recProfile(threadName, state, call :: callStack ,profilingPerThread)
          case StateRegex(newState) => recProfile(threadName, newState, callStack, profilingPerThread)
          case "" =>
            if( callStack.isEmpty )
              recProfile("", "", Nil, profilingPerThread)
            else {
              val root = profilingPerThread.getOrElse(threadName, new CallGraph(threadName, Runnable))
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
      val profiling = profileThread( Source.fromFile(file).getLines() )
      
      println( "10 most active thread:" )
      sortByUsage(profiling).take(10).foreach{ case (name,callGraph) => println("- %s (%d samples)".format(callGraph.name,callGraph.count)) }
      val (firstThread,graph) = sortByUsage(profiling).head
      println("")
      println( "Critical path of thread: " + firstThread )
      graph.criticalPath.foreach{ x => println(" + "+x) }
    }
  }

  private def usage() {
    println("Usage: jstack-profiler <jstack output file>")
  }
}
