#!/bin/bash

jps | grep datavault
BROKER_PID=$(jps | grep datavault-broker | cut -d' ' -f1)
WEBAPP_PID=$(jps | grep datavault-webapp | cut -d' ' -f1)
WORKER_PID=$(jps | grep datavault-worker | cut -d' ' -f1)

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
