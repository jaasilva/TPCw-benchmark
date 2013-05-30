#!/bin/sh

CLASSPATH="out/production/BenchmarkingSuite"
CLASSPATH="${CLASSPATH}:out/production/TPCW_DSTM_Implementation"
CLASSPATH="${CLASSPATH}:libs/log4j-1.2.13.jar"
CLASSPATH="${CLASSPATH}:libs/colt.jar"
CLASSPATH="${CLASSPATH}:libs/jackson-core-asl-1.0.1.jar"
CLASSPATH="${CLASSPATH}:libs/jackson-mapper-asl-1.0.1.jar"

THREADS=$1
REPLICAS=$2
DURATION=$3

echo "#####"
echo "Workload: TPCW"
echo "Threads: ${THREADS}"
echo "Generating for ${REPLICAS} nodes"
echo `date +%H:%M`
echo "#####"

java -Xmx8g -Xms8g -cp $CLASSPATH \
    -Dtribu.replicas=$REPLICAS \
    org.uminho.gsd.benchmarks.benchmark.BenchmarkMain \
        -d dstm -w browsing -t $THREADS -o 262144 -tt 0 -duration $DURATION

# vim:set ts=4 sw=4 et:
