echo "START: `date +%H:%M:%S`"
start=`date +%s`

DIR="./repos/tpcw"
OPS=10000

for comm in jgroups.JGroups #appia.Appia spread.Spread
do
for threads in 2
do
for _data in Simple RoundRobin Random
do
for _groups in 1 2 4 8
do
for run in 1 2 3 4 5
do

echo "###########################"
echo "Benchmark: TPCW, run $run"
echo "Thrs: $threads, Rpls: 8, Groups: $_groups"
echo "Comm: $comm, browsing mix"
echo "DPart: $_data"
echo "Time: `date +%H:%M:%S`"
echo "###########################"

start2=`date +%s`

# RUN MASTER NODE
ssh node1 "cd ${DIR}; ./scripts/run_prep.sh $comm $threads 8 $run $OPS $_groups $_data -p -m > node1.out 2>&1" &

sleep 5

# RUN SLAVE NODES
for node in node2 node3 node4 node5 node6 node7 node8
do
	ssh $node "cd ${DIR}; ./scripts/run_prep.sh $comm $threads 8 $run $OPS $_groups $_data -s 8081 > $node.out 2>&1" &
done

wait
end2=`date +%s`
echo "> $(( ($end2-$start2) ))s"
sleep 10

done
done
done
done
done

############################################################################
echo "-----------------------------------------------------------"

for comm in jgroups.JGroups #appia.Appia spread.Spread
do
for threads in 2
do
for run in 1 2 3 4 5
do

echo "###########################"
echo "Benchmark: TPCW, run $run"
echo "Thrs: $threads, Rpls: 8"
echo "Comm: $comm, browsing mix"
echo "Time: `date +%H:%M:%S`"
echo "###########################"

start2=`date +%s`

# RUN MASTER NODE
ssh node1 "cd ${DIR}; ./scripts/run.sh $comm $threads 8 $run $OPS -p -m > node1.out 2>&1" &

sleep 5

# RUN SLAVE NODES
for node in node2 node3 node4 node5 node6 node7 node8
do
	ssh $node "cd ${DIR}; ./scripts/run.sh $comm $threads 8 $run $OPS -s 8081 > $node.out 2>&1" &
done

wait
end2=`date +%s`
echo "> $(( ($end2-$start2) ))s"
sleep 10

done
done
done

echo "END: `date +%H:%M:%S`"
end=`date +%s`
echo "Duration: $(( ($end-$start)/60 ))min"

