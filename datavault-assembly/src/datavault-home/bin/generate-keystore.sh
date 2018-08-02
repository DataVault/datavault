#!/bin/bash

if [ -z "$DATAVAULT_HOME" ]; then
    echo "DATAVAULT_HOME is not set"
    exit 1
fi

if [ $# != 3 ]; then
    echo "Usage: "
    echo "./generate-keystore.sh <path_to_keystore> <keystore_password>  <keyname1>,<keyname2>,..."
    echo "Example: "
    echo "./generate-keystore.sh /path/to/projects/datavault/docker/keystore/DatavaultKeyStore veryStrongPassword data-encryption-key,ssh-encryption-key"
    exit 1
fi

cd ${DATAVAULT_HOME}/lib

java -Dlog4j.configuration=$DATAVAULT_HOME/config/log4j.properties -cp datavault-common-1.0-SNAPSHOT.jar:./* org.datavaultplatform.common.crypto.Encryption generateSecretKeyAndAddToJCEKS "$@"


