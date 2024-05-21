#!/bin/bash
SCRIPT_DIR=`dirname $0`
# imports the rabbit definitions - this includes, queues, exchanges AND USERS !!!
. $SCRIPT_DIR/setup.sh

echo "importing rabbit_definitions.json"
$SCRIPT_DIR/rabbitmqadmin -u $RABBIT_USER -p $RABBIT_PASS import ./rabbitmq-definitions.json