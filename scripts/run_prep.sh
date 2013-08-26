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
CLASSPATH="${CLASSPATH}:libs/guava-14.0.jar"

INCLUDE="pt.unl.citi.tpcw.*"
INCLUDE="${INCLUDE},org.uminho.gsd.benchmarks.benchmark.BenchmarkMain"
EXCLUDE="java.*,sun.*,org.eclipse.*,org.junit.*,junit.*"
EXCLUDE="${EXCLUDE},net.sf.appia.*"
EXCLUDE="${EXCLUDE},net.sf.jgcs.*"
EXCLUDE="${EXCLUDE},org.jgroups.*"
EXCLUDE="${EXCLUDE},com.google.*"
EXCLUDE="${EXCLUDE},flanagan.*"
EXCLUDE="${EXCLUDE},org.apache.log4j.*"
EXCLUDE="${EXCLUDE},spread.*"
EXCLUDE="${EXCLUDE},org.deuce.trove.*"
EXCLUDE="${EXCLUDE},org.codehaus.jackson.*"
EXCLUDE="${EXCLUDE},cern.jet.*"
EXCLUDE="${EXCLUDE},cern.colt.*"
EXCLUDE="${EXCLUDE},org.uminho.gsd.benchmarks.*"
EXCLUDE="${EXCLUDE},pt.unl.citi.tpcw.util.trove.HashFunctions"
EXCLUDE="${EXCLUDE},pt.unl.citi.tpcw.util.trove.PrimeFinder"

THREADS=$2
REPLICAS=$3
RUN=$4
OPS=$5
GROUPS=$6

_STM=score.SCOReContext
_REP=score.SCOReProtocol
_COMM=$1 #jgroups.JGroups

STM="org.deuce.transaction.${_STM}"
COMM="org.deuce.distribution.groupcomm.${_COMM}GroupCommunication"
REP="org.deuce.distribution.replication.partial.protocol.${_REP}"
ZIP=true
GROUP="TPCW_${WORKERS}_${_REP}_${REPLICAS}_${RUN}"
FNAME="TPCW_t${WORKERS}_${_REP}_${_COMM}_id${SITE}-${REPLICAS}_run${RUN}"
LOG=logs/${FNAME}.res
#DATA_PART=org.deuce.distribution.replication.partitioner.data.RandomDataPartitioner
#DATA_PART=org.deuce.distribution.replication.partitioner.data.SimpleDataPartitioner
DATA_PART=org.deuce.distribution.replication.partitioner.data.RoundRobinDataPartitioner

echo "#####"
echo "Benchmark: TPCW, run ${RUN}"
echo "Threads: ${THREADS}"
echo "Protocol: ${_REP}, site ${SITE} of ${REPLICAS}"
echo "Comm: ${_COMM}"
echo `date +%H:%M`
echo "#####"

shift 6

java -Xmx1g -Xms1g -cp $CLASSPATH -javaagent:libs/deuceAgent.jar \
    -Dorg.deuce.transaction.contextClass=$STM \
    -Dorg.deuce.exclude=$EXCLUDE \
    -Dorg.deuce.include=$INCLUDE \
    -Dtribu.groupcommunication.class=$COMM \
    -Dtribu.groupcommunication.group=$GROUP \
    -Dtribu.replicas=$REPLICAS \
    -Dtribu.distributed.protocolClass=$REP \
    -Dtribu.serialization.compress=$ZIP \
    -Dtribu.distributed.DataPartitionerClass=$DATA_PART \
    -Dtribu.distributed.PartialReplicationMode=true \
	-Dtribu.groups=$GROUPS \
    org.uminho.gsd.benchmarks.benchmark.BenchmarkMain \
        -d dstm -w browsing -t $THREADS -o $OPS -tt 0 $@ # -p -m ou -s <port>

# -w browsing | -w consistency | -w ordering

# vim:set ts=4 sw=4 et:
