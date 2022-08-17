#!/bin/bash
SCRIPT_DIR=`dirname $0`
# deletes messages from the RabbitMQ queue - 'datavault'
. $SCRIPT_DIR/setup.sh
echo "Purging Queue 'datavault'..."
$SCRIPT_DIR/rabbitmqadmin purge queue -u rabbit -p $RABBIT_PASS name=datavault
