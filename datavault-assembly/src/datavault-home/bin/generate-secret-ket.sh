#!/bin/bash

if [ -z "$DATAVAULT_HOME" ]; then
    echo "DATAVAULT_HOME is not set"
    exit 1
fi

cd ${DATAVAULT_HOME}/lib

java -Dlog4j.configuration=$DATAVAULT_HOME/config/log4j.properties -cp datavault-common-1.0-SNAPSHOT.jar:./* org.datavaultplatform.common.crypto.Encryption generateSecretKey


