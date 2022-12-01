#!/bin/bash

SCRIPT_DIR=`dirname $0`
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)
BROKER_PID=$(find $PROJECT_ROOT -name 'dv-broker-shutdown.pid' -exec cat {} \;)
WEBAPP_PID=$(find $PROJECT_ROOT -name 'dv-webapp-shutdown.pid' -exec cat {} \;)
WORKER_PID=$(find $PROJECT_ROOT -name 'dv-worker-shutdown.pid' -exec cat {} \;)

if [ ! -z "$WEBAPP_PID" ]; then
    echo "Stopping WebApp with PID $WEBAPP_PID"
    kill $WEBAPP_PID
fi
if [ ! -z "$BROKER_PID" ]; then
    echo "Stopping Broker with PID $BROKER_PID"
    kill $BROKER_PID
fi
if [ ! -z "$WORKER_PID" ]; then
    echo "Stopping Worker with PID $WORKER_PID"
    kill $WORKER_PID
fi
