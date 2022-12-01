#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "BROKER $HOST:$PORT_BROKER actuator/customtime is ";
curl -u bactor:bactorpass $CURL_OPTS http://$HOST:$PORT_BROKER/actuator/customtime | jq

/bin/echo "WEBAPP $HOST:$PORT_WEBAPP actuator/customtime is ";
curl -u wactor:wactorpass $CURL_OPTS http://$HOST:$PORT_WEBAPP/actuator/customtime | jq

/bin/echo "WORKER $HOST:$PORT_WORKER actuator/customtime is ";
curl -u wactu:wactupass $CURL_OPTS http://$HOST:$PORT_WORKER/actuator/customtime | jq

