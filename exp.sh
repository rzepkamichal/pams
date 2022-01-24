SERVER_HOST="10.128.0.4" # load balancer or single server
SERVER_PORT="25000"
SERVER1_PORT="25001"
SERVER2_PORT="25002"

NUM_CLIENTS=$1
MULTIPLIER=$2

INPUT_FILE=1
DOC_SIZE=16

NUM_THREADS=4
CLEAN=true

rm *.log

nohup ssh s1 "killall java; sleep 1; java -jar WoCoServer.jar 0.0.0.0 $SERVER1_PORT $CLEAN $NUM_THREADS" > server1.log 2>server1.log &
nohup ssh s2 "killall java; sleep 1; java -jar WoCoServer.jar 0.0.0.0 $SERVER2_PORT $CLEAN $NUM_THREADS" > server2.log 2>server2.log &
nohup ssh lb "killall java; sleep 1; java -jar WoCoBalancer.jar 0.0.0.0 $SERVER_PORT config 20" > lb.log 2>lb.log &

sleep 2

for x in `seq 2 $1`
do

if [[ $((x%2)) -eq 0 ]]
then		
	ssh c1 "java -jar WoCoClient.jar $SERVER_HOST $SERVER_PORT $DOC_SIZE $MULTIPLIER $INPUT_FILE" > client$x.log &
else
	ssh c2 "java -jar WoCoClient.jar $SERVER_HOST $SERVER_PORT $DOC_SIZE $MULTIPLIER $INPUT_FILE" > client$x.log &
fi

done

ssh c1 "java -jar WoCoClient.jar $SERVER_HOST $SERVER_PORT $DOC_SIZE $MULTIPLIER $INPUT_FILE" > client1.log

grep "Total " *.log | awk '{sum += $7} END {print sum}'
