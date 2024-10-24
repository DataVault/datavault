#!/bin/bash
SCRIPT_DIR=`dirname $0`
# lists the RabbitMQ users.
. $SCRIPT_DIR/setup.sh
$SCRIPT_DIR/rabbitmqadmin list users -u $RABBIT_USER -p $RABBIT_PASS

