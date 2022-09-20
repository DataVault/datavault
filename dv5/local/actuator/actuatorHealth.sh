#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh


/bin/echo -n "WEBAPP $HOST:$PORT_WEBAPP health is "; curl $CURL_OPTS http://$HOST:$PORT_WEBAPP/actuator/health  | jq '[.status][0]'
/bin/echo -n "BROKER $HOST:$PORT_BROKER health is "; curl $CURL_OPTS http://$HOST:$PORT_BROKER/actuator/health | jq '[.status][0]'
/bin/echo -n "WORKER $HOST:$PORT_WORKER health is "; curl $CURL_OPTS http://$HOST:$PORT_WORKER/actuator/health | jq '[.status][0]'