#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "BROKER $BROKER_HOST:$PORT_BROKER actuator/sftpfilestores is ";
curl -k -u bactor:bactorpass $CURL_OPTS https://$BROKER_HOST:$PORT_BROKER/actuator/sftpfilestores | jq


