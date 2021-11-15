SERVER_HOST="127.0.0.1"
SERVER_PORT="12345"
LOCATION="jars"
THREADS=1
CLEAN=0
DOC_SIZE=16
FILE_SUFF=1
MULTIPLIER=$2


### TODO: To perform repetitions of the experiment, you might want to wrap the code below in a for loop.



#echo "* COPY JARS TO $SERVER_HOST"
#scp ./jars/*.jar $SERVER_HOST:"$LOCATION/"

#echo "* START SERVER ON $SERVER_HOST"
(java -jar $LOCATION/WoCoServer.jar $SERVER_HOST $SERVER_PORT $CLEAN $THREADS > ./server.log ) &

sleep 1

#echo "* START CLIENTS"

for x in `seq 2 $1`
do
	 
	 (java -jar ./jars/WoCoClient.jar $SERVER_HOST $SERVER_PORT $DOC_SIZE $MULTIPLIER $FILE_SUFF > ./client$x.log) &
done
java -jar ./jars/WoCoClient.jar $SERVER_HOST $SERVER_PORT $DOC_SIZE $MULTIPLIER $FILE_SUFF > ./client1.log

sleep 1

#echo "* CLEANUP"
killall java


grep "Total " *.log | awk '{sum += $7} END {print sum}'

rm *.log