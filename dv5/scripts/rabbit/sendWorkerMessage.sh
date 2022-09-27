#!/bin/bash
SCRIPT_DIR=`dirname $0`
export MESSAGE_FILE=$1
if [[ ! -f $MESSAGE_FILE ]]; then
  echo "The file [$1] does not exist";
  exit 1;
fi
# sends the specified json message file to the RabbitMQ queue - 'datavault'
. ./setup.sh
$SCRIPT_DIR/purgeWorkerQueue.sh
MSG_ID="sendWorkerMessage.sh@$(date +%T)"
echo "Sending message to Queue named 'datavault'"

cat $MESSAGE_FILE | $SCRIPT_DIR/rabbitmqadmin publish \
 routing_key="datavault" -u $RABBIT_USER -p $RABBIT_PASS \
 properties="{\"message_id\":\"$MSG_ID\"}"

