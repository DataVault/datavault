#!/bin/bash

if [ -z "$DATAVAULT_HOME" ]; then
    echo "DATAVAULT_HOME is not set"
    exit 1
fi

if [ $# != 1 ]; then
    echo "Usage: "
    echo "./generate-keystore.sh <path_to_json_file> <keystore_password>  <keyname1>,<keyname2>,..."
    echo "Example of json: "
    echo -e "{\n\
        \"path\":\"/path/to/projects/datavault/docker/keystore/DatavaultKeyStore\",\n\
        \"password\":\"veryStrongPassword\",\n\
        \"key_aliases\":[\"data-encryption-key\",\"ssh-encryption-key\"]\n\
    }"

    exit 1
fi

cd ${DATAVAULT_HOME}/lib

java -Dlog4j.configuration=$DATAVAULT_HOME/config/log4j.properties -cp datavault-common-1.0-SNAPSHOT.jar:./* org.datavaultplatform.common.crypto.Encryption generateSecretKeyAndAddToJCEKS "$@"


