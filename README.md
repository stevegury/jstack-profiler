[![Build Status](https://secure.travis-ci.org/stevegury/jstack-profiler.png)](http://travis-ci.org/stevegury/jstack-profiler)

# Jstack Profiler

Simple profiler that takes concatenation of jstack's output as input.
It's useful, when you want to find out what's going on a production server that you can't stop or profile (with classic profiler).

# How to compile

	$ sbt assembly

It generates a big jar file in the target directory


# How to use

First generate a full thread dump output every n seconds (60 sec in this example), append those thread dumps in a file (jstack.log in this example).
Ctrl+C to terminate

	$ while true ; do jstack PID >> jstack.log ; sleep 60 ; done

Then launch the analyze with:

	$ cat jstack.log | java -jar target/scala-2.11/jstack-profiler-assembly-0.2.jar

Here is an example of the output:

	Runnable methods breakout
	-------------------------
	rank    time    name
	0       82.50%  java.lang.Thread.run(Thread.java:722)
	1       80.00%  sun.nio.ch.SelectorImpl.select(SelectorImpl.java:98)
	2       11.79%  org.jboss.netty.util.internal.DeadLockProofWorker$1.run(DeadLockProofWorker.java:42)
	3       11.43%  sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:69)
	4       2.86%   org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:582)
	5       2.50%   sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:228)
	6       1.79%   org.springframework.jdbc.core.JdbcTemplate$3.doInPreparedStatement(JdbcTemplate.java:844)
	7       0.71%   java.net.SocketInputStream.socketRead0(Native Method)
	8       0.36%   com.google.gson.stream.JsonWriter.string(JsonWriter.java:535)

	Blocked/Waiting methods breakout
	--------------------------------
	rank    time    name
	0       41.96%  com.example.service.run(Servie.java:123)
	1       24.22%  sun.misc.Unsafe.park(Native Method)
	2       12.61%  java.lang.Thread.run(Thread.java:722)
	3       11.83%  java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1043)
	4       10.27%  java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:226)
	5       5.80%   java.util.concurrent.LinkedBlockingDeque.takeFirst(LinkedBlockingDeque.java:440)
	6       4.13%   com.google.common.cache.LocalCache.get(LocalCache.java:3980)
	7       2.68%   com.mchange.v2.async.ThreadPoolAsynchronousRunner$PoolThread.run(ThreadPoolAsynchronousRunner.java:534)
	8       1.56%   java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:807)
	9       1.45%   java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:907)
	10      1.34%   java.util.concurrent.SynchronousQueue$TransferStack.awaitFulfill(SynchronousQueue.java:424)
	11      0.78%   com.google.common.cache.LocalCache$Segment.waitForLoadingValue(LocalCache.java:2333)
	12      0.11%   java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:118)
