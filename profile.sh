#!/bin/sh

if [ -z "$1" ]
then
    >&2 echo "Usage: $0 <PID> [PROFILING_IN_SECONDS (default=60)] [SAMPLE_PERIOD_IN_SECOND (default=5)]"
    exit 1
fi

PID=$1
PROFILING_IN_SECONDS=${2:-120}
SAMPLE_PERIOD_IN_SECOND=${3:-5}

JAR="target/scala-2.12/jstack-profiler-assembly-0.3.jar"

if [ ! -f $JAR ]
then
  >&2 echo "Jar '$JAR' missing, rebuilding it."
  sbt assembly
fi

if [ ! -f $JAR ]
then
  >&2 echo "Unable to build Jar '$JAR'."
  exit 1
fi

ITERATIONS=$(($PROFILING_IN_SECONDS/$SAMPLE_PERIOD_IN_SECOND))
MIN_ITERATIONS=5
if [ $ITERATIONS -lt $MIN_ITERATIONS ]
then
  MIN_SAMPLE=$(($PROFILING_IN_SECONDS / $MIN_ITERATIONS))
  MIN_DURATION=$(($SAMPLE_PERIOD_IN_SECOND * $MIN_ITERATIONS))
  >&2 echo "Sampling period ($SAMPLE_PERIOD_IN_SECOND) is too big for the profile duration ($PROFILING_IN_SECONDS)"
  >&2 echo "Either increase the profile duration above $MIN_DURATION s or decrease sampling period below $MIN_SAMPLE s"
  exit 1
fi

echo "Collecting $ITERATIONS stack traces of process $PID"
OUTPUT=jstack.$(date +%s)
i=0
while [ $i -lt $ITERATIONS ]
do
  jstack $1 >> $OUTPUT
  printf "."
  sleep $SAMPLE_PERIOD_IN_SECOND
  i=$[$i + 1]
done
echo ""
echo "Processing $ITERATIONS stack traces..."
cat $OUTPUT | java -jar $JAR
