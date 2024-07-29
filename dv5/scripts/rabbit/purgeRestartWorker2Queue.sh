#!/bin/bash
SCRIPT_DIR=`dirname $0`
# deletes messages from the RabbitMQ queue - 'restart-worker-2'
. $SCRIPT_DIR/setup.sh
echo "Purging Queue 'restart-worker-2'..."
$SCRIPT_DIR/rabbitmqadmin purge queue -u rabbit -p $RABBIT_PASS name=restart-worker-2
