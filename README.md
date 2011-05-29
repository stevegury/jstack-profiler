# Jstack Profiler

Simple profiler that take concatenation of jstack's output as input

# How to use

First generate an full thread dump output every n seconds (60 sec in this example), append those thread dump in a file (jstack.log in this example). 
Ctrl+C to terminate

	$ while true ; do jstack PID >> jstack.log ; sleep 60 ; done

Then launch the analyze with:

	$ sbt "profiler jstack.log"


