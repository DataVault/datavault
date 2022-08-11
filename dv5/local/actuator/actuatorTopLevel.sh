#!/usr/bin/env sh

BASEDIR=$(dirname $0)
. $BASEDIR/setup.sh

/bin/echo "WEBAPP $HOST:$PORT_WEBAPP actuator/ is ";
curl -u wactor:wactorpass $CURL_OPTS http://$HOST:$PORT_WEBAPP/actuator/ | jq

/bin/echo "BROKER $HOST:$PORT_BROKER actuator/ is ";
curl -u bactor:bactorpass $CURL_OPTS http://$HOST:$PORT_BROKER/actuator/ | jq

/bin/echo "WORKER $HOST:$PORT_WORKER actuator/ is ";
curl -u wactu:wactupass $CURL_OPTS http://$HOST:$PORT_WORKER/actuator/ | jq
