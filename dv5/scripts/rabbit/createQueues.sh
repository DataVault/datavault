#!/bin/bash
# creates the 2 RabbitMQ queues - 'datavault' and 'datavaul-event'
SCRIPT_DIR=`dirname $0`
. $SCRIPT_DIR/setup.sh
echo "Creating Queue 'datavault'..."
$SCRIPT_DIR/rabbitmqadmin declare queue -u $RABBIT_USER -p $RABBIT_PASS name=datavault durable=true  arguments='{"x-max-priority":2}'
echo "Creating Queue 'datavault-event'..."
$SCRIPT_DIR/rabbitmqadmin declare queue -u $RABBIT_USER -p $RABBIT_PASS name=datavault-event durable=true

echo "Creating Queue 'restart-worker-1'..."
$SCRIPT_DIR/rabbitmqadmin declare queue -u $RABBIT_USER -p $RABBIT_PASS name=restart-worker-1 durable=true

echo "Creating Queue 'restart-worker-2'..."
$SCRIPT_DIR/rabbitmqadmin declare queue -u $RABBIT_USER -p $RABBIT_PASS name=restart-worker-2 durable=true

echo "Creating Queue 'restart-worker-3'..."
$SCRIPT_DIR/rabbitmqadmin declare queue -u $RABBIT_USER -p $RABBIT_PASS name=restart-worker-3 durable=true
