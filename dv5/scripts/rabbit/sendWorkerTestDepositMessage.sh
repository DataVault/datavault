#!/bin/bash
SCRIPT_DIR=`dirname $0`
# sends a TEST DEPOSIT message to the RabbitMQ queue - 'datavault'
# IMPORTANT - Assumes the following 2 directories exist:
# IMPORTANT - 1. /tmp/dv/src/src-path-1
# IMPORTANT - 2. /tmp/dv/dest/
# IMPORTANT - AND that there are some files in /tmp/dv/src/src-path-1
. ./setup.sh
$SCRIPT_DIR/purgeWorkerQueue.sh
MSG_ID="sendWorkerTestDepositMessage.sh@$(date +%T)"
echo "Sending test DEPOSIT message message to Queue named 'datavault'"
BAGID=$(date +"%d%m%Y-%H%M%S")
BASE_DIR="/tmp/dv"
SRC_DIR="$BASE_DIR/src"
DEST_DIR="$BASE_DIR/dest"
echo "BASE_DIR $BASE_DIR"
echo "SRC_DIR  $SRC_DIR"
echo "DEST_DIR $DEST_DIR"
if [ ! -d "$BASE_DIR" ]; then
  echo "$BASE_DIR does not exist"
  exit 1
fi
if [ ! -d "$SRC_DIR/src-path-1" ]; then
  echo "$SRC_DIR/src-path-1 does not exist"
  exit 1
fi
if [ ! "$(ls -A $SRC_DIR/src-path-1)" ]; then
  echo "$SRC_DIR/src-path-1 is empty "
  exit 1
fi
if [ ! -d "$DEST_DIR" ]; then
  echo "$DEST_DIR does not exist"
  exit 1
fi
export DEPOSIT_REQUEST=$( cat $SCRIPT_DIR/sampleDepositMessage.json | \
sed "s|bf73a7f5-42d1-4c3f-864a-a171af8373d4|$BAGID|g" | \
sed "s|/tmp/dv/src|$SRC_DIR|g" | \
sed "s|/tmp/dv/dest|$DEST_DIR|g" )
echo $DEPOSIT_REQUEST | jq
echo $DEPOSIT_REQUEST | $SCRIPT_DIR/rabbitmqadmin publish \
 routing_key="datavault" -u $RABBIT_USER -p $RABBIT_PASS \
 properties="{\"message_id\":\"$MSG_ID\"}"

