#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "WEBAPP $HOST:$PORT_WEBAPP info is ";
curl -k $CURL_OPTS https://$WEBAPP_HOST:$PORT_WEBAPP/actuator/info | jq

/bin/echo "BROKER $HOST:$PORT_BROKER info is ";
curl -k $CURL_OPTS https://$BROKER_HOST:$PORT_BROKER/actuator/info | jq

/bin/echo "WORKER $HOST:$PORT_WORKER info is ";
curl -k $CURL_OPTS https://$WORKER_HOST:$PORT_WORKER/actuator/info | jq

