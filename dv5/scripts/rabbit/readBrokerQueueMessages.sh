#!/bin/bash
SCRIPT_DIR=`dirname $0`
# reads broker messages from the queue - 'datavault-event'.
# LEAVES THE MESSAGES ON THE QUEUE.
. ./setup.sh
PAYLOAD_FILE="brokerQueueMessages-$(date +%T).txt"
echo "Reading Broker Queue Message from Q[datavault-event] to file named [$PAYLOAD_FILE]"
BASE_Q_URL="http://localhost:15672/api/queues/%2F/datavault-event"
COUNT=$(curl -s -u $RABBIT_USER:$RABBIT_PASS $BASE_Q_URL| jq -r '.messages')
echo COUNT is $COUNT
curl -s -u $RABBIT_USER:$RABBIT_PASS \
 -H "content-type:application/json" \
 -d "{\"count\":$COUNT,\"requeue\":true,\"encoding\":\"auto\",\"truncate\":50000,\"ackmode\":\"ack_requeue_true\"}" \
 -X POST $BASE_Q_URL/get | jq -r 'map(.payload | fromjson) | to_entries | map({index:.key, number:(1 + .key), value:.value})'
echo "read ${COUNT} messages from [datavault-event] queue"