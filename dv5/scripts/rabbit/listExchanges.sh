#!/bin/bash
SCRIPT_DIR=`dirname $0`
# lists the RabbitMQ exchanges.
. $SCRIPT_DIR/setup.sh
$SCRIPT_DIR/rabbitmqadmin list exchanges -u $RABBIT_USER -p $RABBIT_PASS

