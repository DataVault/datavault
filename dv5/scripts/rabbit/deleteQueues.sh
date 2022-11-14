#!/bin/bash
SCRIPT_DIR=`dirname $0`
# deletes the 2 RabbitMQ queues - 'datavault' and 'datavaul-event'
. $SCRIPT_DIR/setup.sh

echo "Deleting Queue 'datavault'..."
$SCRIPT_DIR/rabbitmqadmin delete queue -u $RABBIT_USER -p $RABBIT_PASS name=datavault

echo "Deleting Queue 'datavault-event'..."
$SCRIPT_DIR/rabbitmqadmin delete queue -u $RABBIT_USER -p $RABBIT_PASS name=datavault-event

