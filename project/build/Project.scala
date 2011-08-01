import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {

  def forkDebug = forkRun(None, List("-Xdebug", "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005","-Xmx1024m") )
  def forkNoDebug = forkRun(None, List("-Xmx1024m") )

  override def fork = forkNoDebug

  lazy val logFollower = task { args =>
    logFolowerConstructor(args)
  }

  def logFolowerConstructor( args : Array[String] ) = task {
    runTask( Some("LogFollower"), runClasspath, args.mkString(" ")).run
  }.dependsOn(compile)


  lazy val profiler = task { args =>
    profilerConstructor( args(0) )
  }

  def profilerConstructor( fileName : String ) = task {
    runTask( Some("Profiler"), runClasspath, fileName).run
  }.dependsOn(compile)

}
