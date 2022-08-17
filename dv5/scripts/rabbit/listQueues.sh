#!/bin/bash
SCRIPT_DIR=`dirname $0`
# lists the RabbitMQ queues.
. $SCRIPT_DIR/setup.sh
$SCRIPT_DIR/rabbitmqadmin list queues -u $RABBIT_USER -p $RABBIT_PASS

