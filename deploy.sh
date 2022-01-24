LOCATION_LOCAL=jars
LOCATION_REMOTE=/home

scp $LOCATION_LOCAL/WoCoClient.jar c1:$LOCATION_REMOTE
scp ./input1.html c1:$LOCATION_REMOTE
scp $LOCATION_LOCAL/WoCoClient.jar c2:/home/rzemic
scp ./input1.html c2:$LOCATION_REMOTE
scp $LOCATION_LOCAL/WoCoClient.jar c2:/home/rzemic
scp $LOCATION_LOCAL/WoCoBalancer.jar lb:$LOCATION_REMOTE
scp ./config lb:$LOCATION_REMOTE
scp $LOCATION_LOCAL/WoCoServer.jar s1:$LOCATION_REMOTE
scp $LOCATION_LOCAL/WoCoServer.jar s2:$LOCATION_REMOTE


