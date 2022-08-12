#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "WEBAPP $HOST:$PORT_WEBAPP info is ";
curl $CURL_OPTS http://$HOST:$PORT_WEBAPP/actuator/info | jq

/bin/echo "BROKER $HOST:$PORT_BROKER info is ";
curl $CURL_OPTS http://$HOST:$PORT_BROKER/actuator/info | jq

/bin/echo "WORKER $HOST:$PORT_WORKER info is ";
curl $CURL_OPTS http://$HOST:$PORT_WORKER/actuator/info | jq

