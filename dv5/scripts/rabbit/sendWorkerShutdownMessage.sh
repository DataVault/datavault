#!/bin/bash
SCRIPT_DIR=`dirname $0`
# sends a hi priority shutdown message to the RabbitMQ queue - 'datavault'
. ./setup.sh
MSG_ID="sendWorkerShutdownMessage.sh@$(date +%T)"
echo "Sending shutdown message to Queue named 'datavault'"
$SCRIPT_DIR/rabbitmqadmin publish routing_key="datavault" -u $RABBIT_USER -p $RABBIT_PASS \
 properties="{\"priority\":2,\"message_id\":\"$MSG_ID\"}" \
 payload="shutdown"
