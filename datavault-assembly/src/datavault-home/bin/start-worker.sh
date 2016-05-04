#!/bin/bash

if [ -z "$DATAVAULT_HOME" ]; then
    echo "DATAVAULT_HOME is not set"
    exit 1
fi

cd ${DATAVAULT_HOME}/lib

java -cp datavault-worker-1.0-SNAPSHOT.jar:./* org.datavaultplatform.worker.WorkerManager &


