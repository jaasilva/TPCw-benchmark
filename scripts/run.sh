#!/bin/sh

CLASSPATH="out/production/BenchmarkingSuite"
CLASSPATH="${CLASSPATH}:out/production/TPCW_DSTM_Implementation"
CLASSPATH="${CLASSPATH}:libs/log4j-1.2.13.jar"
CLASSPATH="${CLASSPATH}:libs/colt.jar"
CLASSPATH="${CLASSPATH}:libs/jackson-core-asl-1.0.1.jar"
CLASSPATH="${CLASSPATH}:libs/jackson-mapper-asl-1.0.1.jar"
CLASSPATH="${CLASSPATH}:libs/deuceAgent.jar"
CLASSPATH="${CLASSPATH}:libs/appia-core-4.1.2.jar"
CLASSPATH="${CLASSPATH}:libs/appia-groupcomm-4.1.2.jar"
CLASSPATH="${CLASSPATH}:libs/flanagan.jar"
CLASSPATH="${CLASSPATH}:libs/jgcs-0.6.1.jar"
CLASSPATH="${CLASSPATH}:libs/jgroups-3.3.0.Final.jar"
CLASSPATH="${CLASSPATH}:libs/spread-4.2.0.jar"

INCLUDE="pt.unl.citi.tpcw.*"
INCLUDE="${INCLUDE},org.uminho.gsd.benchmarks.benchmark.BenchmarkMain"
EXCLUDE="java.*,sun.*,org.eclipse.*,org.junit.*,junit.*"
EXCLUDE="${EXCLUDE},net.sf.appia.*"
EXCLUDE="${EXCLUDE},net.sf.jgcs.*"
EXCLUDE="${EXCLUDE},org.jgroups.*"
EXCLUDE="${EXCLUDE},flanagan.*"
EXCLUDE="${EXCLUDE},org.apache.log4j.*"
EXCLUDE="${EXCLUDE},spread.*"
EXCLUDE="${EXCLUDE},org.deuce.trove.*"
EXCLUDE="${EXCLUDE},org.codehaus.jackson.*"
EXCLUDE="${EXCLUDE},cern.jet.*"
EXCLUDE="${EXCLUDE},org.uminho.gsd.benchmarks.helpers.*"
EXCLUDE="${EXCLUDE},pt.unl.citi.tpcw.util.trove.HashFunctions"
EXCLUDE="${EXCLUDE},pt.unl.citi.tpcw.util.trove.PrimeFinder"

SITE=$2
THREADS=1
WORKERS=$3
REPLICAS=$4
RUN=$5

_STM=tl2.Context
_REP=nonvoting.NonVoting
_COMM=$1

STM="org.deuce.transaction.${_STM}"
COMM="org.deuce.distribution.groupcomm.${_COMM}GroupCommunication"
REP="org.deuce.distribution.replication.full.protocol.${_REP}"
ZIP=true
GROUP="TPCW_${WORKERS}_${_REP}_${REPLICAS}_${RUN}"
FNAME="TPCW_t${WORKERS}_${_REP}_${_COMM}_id${SITE}-${REPLICAS}_run${RUN}"
LOG=logs/${FNAME}.res

echo "#####"
echo "Benchmark: TPCW, run ${RUN}"
echo "Threads: ${WORKERS}"
echo "Protocol: ${_REP}, site ${SITE} of ${REPLICAS}"
echo "Comm: ${_COMM}"
echo `date +%H:%M`
echo "#####"

shift 5

java -Xmx8g -Xms8g -cp $CLASSPATH -javaagent:libs/deuceAgent.jar \
    -Dorg.deuce.transaction.contextClass=$STM \
    -Dorg.deuce.exclude=$EXCLUDE \
    -Dorg.deuce.include=$INCLUDE \
    -Dtribu.groupcommunication.class=$COMM \
    -Dtribu.groupcommunication.group=$GROUP \
    -Dtribu.site=$SITE \
    -Dtribu.replicas=$REPLICAS \
    -Dtribu.workers=$WORKERS \
    -Dtribu.distributed.protocolClass=$REP \
    -Dtribu.serialization.compress=$ZIP \
    org.uminho.gsd.benchmarks.benchmark.BenchmarkMain \
        -d dstm -p -w browsing -t 1 -o 8192 -tt 0 $@

# vim:set ts=4 sw=4 et:
