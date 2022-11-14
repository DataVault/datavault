#!/bin/bash
SCRIPT_DIR=`dirname $0`
# deletes messages from the RabbitMQ queue - 'datavault-event'
. $SCRIPT_DIR/setup.sh
echo "Purging Queue 'datavault-event'..."
$SCRIPT_DIR/rabbitmqadmin purge queue -u $RABBIT_USER -p $RABBIT_PASS name=datavault-event

