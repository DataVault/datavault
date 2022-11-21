#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "WEBAPP $HOST:$PORT_WEBAPP actuator/ is ";
curl -k -u wactor:wactorpass $CURL_OPTS https://$WEBAPP_HOST:$PORT_WEBAPP/actuator/ | jq

/bin/echo "BROKER $HOST:$PORT_BROKER actuator/ is ";
curl -k -u bactor:bactorpass $CURL_OPTS https://$BROKER_HOST:$PORT_BROKER/actuator/ | jq

/bin/echo "WORKER $HOST:$PORT_WORKER actuator/ is ";
curl -k -u wactu:wactupass $CURL_OPTS https://$WORKER_HOST:$PORT_WORKER/actuator/ | jq
