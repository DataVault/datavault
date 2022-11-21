#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "BROKER $BROKER_HOST:$PORT_BROKER actuator/customtime is ";
# NOTE : we have to give curl the -k option when we are using self-signed certs
curl -k -u bactor:bactorpass $CURL_OPTS https://$BROKER_HOST:$PORT_BROKER/actuator/customtime | jq


