# Jstack Profiler

Simple profiler that takes concatenation of jstack's output as input.
It's useful, when you want to find out what's going on a production server that you can't stop or profile (with classic profiler).

# How to use

First generate a full thread dump output every n seconds (60 sec in this example), append those thread dumps in a file (jstack.log in this example). 
Ctrl+C to terminate

	$ while true ; do jstack PID >> jstack.log ; sleep 60 ; done

Then launch the analyze with:

	$ sbt "profiler jstack.log"

