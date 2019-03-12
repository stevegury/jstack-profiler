[![Build Status](https://secure.travis-ci.org/stevegury/jstack-profiler.png)](http://travis-ci.org/stevegury/jstack-profiler)

# Jstack Profiler

Simple profiler that takes concatenation of jstack's output as input.
It's useful, when you want to find out what's going on a production server that you can't stop or profile (with classic profiler).

# Release

The latest release is 0.3, you can find the jar here.

# How to compile

	$ sbt assembly

It generates a big jar file in the target directory

# How to use

## With the script

	$ ./profile.sh 20399 180 10
	Collecting 18 stack traces of process 20399
	..................
	Processing 18 stack traces...
	Runnable methods breakout
	-------------------------
	rank	time	name
	0	100.00%	java.lang.Thread.run(Thread.java:745)
	1	97.30%	java.net.ServerSocket.implAccept(ServerSocket.java:545)
	2	97.30%	java.net.PlainSocketImpl.socketAccept(Native Method)
	3	97.30%	sun.rmi.transport.tcp.TCPTransport$AcceptLoop.executeAcceptLoop(TCPTransport.java:400)
	4	97.30%	java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
	5	97.30%	java.net.ServerSocket.accept(ServerSocket.java:513)
	6	97.30%	sun.rmi.transport.tcp.TCPTransport$AcceptLoop.run(TCPTransport.java:372)
	7	2.70%	java.io.FilterInputStream.read(FilterInputStream.java:83)
	8	2.70%	java.security.AccessController.doPrivileged(Native Method)
	9	2.70%	java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
	10	2.70%	java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	11	2.70%	java.io.BufferedInputStream.read(BufferedInputStream.java:265)
	12	2.70%	sun.rmi.transport.tcp.TCPTransport.handleMessages(TCPTransport.java:550)
	13	2.70%	sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.lambda$run$0(TCPTransport.java:683)
	14	2.70%	sun.rmi.transport.tcp.TCPTransport$ConnectionHandler$$Lambda$5/1144889983.run(Unknown Source)
	15	2.70%	java.io.BufferedInputStream.fill(BufferedInputStream.java:246)
	16	2.70%	sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run(TCPTransport.java:682)
	17	2.70%	java.net.SocketInputStream.read(SocketInputStream.java:171)
	18	2.70%	java.net.SocketInputStream.socketRead0(Native Method)
	19	2.70%	java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	20	2.70%	java.net.SocketInputStream.read(SocketInputStream.java:141)
	21	2.70%	sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run0(TCPTransport.java:826)

	Blocked/Waiting methods breakout
	--------------------------------
	rank	time	name
	0	28.57%	java.lang.Thread.run(Thread.java:745)
	1	14.29%	sun.misc.GC$Daemon.run(GC.java:117)
	2	14.29%	java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:143)
	3	14.29%	java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:809)
	4	14.29%	org.jetbrains.idea.maven.server.RemoteMavenServer.main(RemoteMavenServer.java:22)
	5	14.29%	java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1067)
	6	14.29%	java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	7	14.29%	java.lang.Object.wait(Native Method)
	8	14.29%	java.lang.Object.wait(Object.java:502)
	9	14.29%	java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(ScheduledThreadPoolExecutor.java:1093)
	10	14.29%	java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
	11	14.29%	com.intellij.execution.rmi.RemoteServer.start(RemoteServer.java:94)
	12	14.29%	sun.rmi.transport.ObjectTable$Reaper.run(ObjectTable.java:351)
	13	14.29%	java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:209)
	14	14.29%	java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1127)
	15	14.29%	sun.misc.Unsafe.park(Native Method)
	16	14.29%	java.lang.ref.Reference.tryHandlePending(Reference.java:191)
	17	14.29%	java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(AbstractQueuedSynchronizer.java:2078)
	18	14.29%	sun.rmi.transport.DGCClient$EndpointEntry$RenewCleanThread.run(DGCClient.java:553)
	19	14.29%	java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:164)
	20	14.29%	java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)


## Manually

First generate a full thread dump output every n seconds (60 sec in this example), append those thread dumps in a file (jstack.log in this example).
Ctrl+C to terminate

	$ while true ; do jstack PID >> jstack.log ; sleep 60 ; done

Then launch the analyze with:

	$ cat jstack.log | java -jar target/scala-2.12/jstack-profiler-assembly-0.3.jar

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
