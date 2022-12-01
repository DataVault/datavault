#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "BROKER $BROKER_HOST:$PORT_BROKER actuator/customtime is ";
# NOTE : we have to give curl the -k option when we are using self-signed certs
curl -k -u bactor:bactorpass $CURL_OPTS https://$BROKER_HOST:$PORT_BROKER/actuator/customtime | jq

/bin/echo "WEBAPP $WEBAPP_HOST:$PORT_WEBAPP actuator/customtime is ";
curl -k -u wactor:wactorpass $CURL_OPTS https://$WEBAPP_HOST:$PORT_WEBAPP/actuator/customtime | jq

/bin/echo "WORKER $WORKER_HOST:$PORT_WORKER actuator/customtime is ";
curl -k -u wactu:wactupass $CURL_OPTS https://$WORKER_HOST:$PORT_WORKER/actuator/customtime | jq
