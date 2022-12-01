#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo -n "WEBAPP $WEBAPP_HOST:$PORT_WEBAPP health is "; curl -k $CURL_OPTS https://$WEBAPP_HOST:$PORT_WEBAPP/actuator/health  | jq '[.status][0]'
/bin/echo -n "BROKER $BROKER_HOST:$PORT_BROKER health is "; curl -k $CURL_OPTS https://$BROKER_HOST:$PORT_BROKER/actuator/health | jq '[.status][0]'
/bin/echo -n "WORKER $WORKER_HOST:$PORT_WORKER health is "; curl -k $CURL_OPTS https://$WORKER_HOST:$PORT_WORKER/actuator/health | jq '[.status][0]'
