#!/bin/bash

# NOTE : this script will create a keystore if it does not exist.
# If the keystore exists AND you want to update the keys in in it, the supplied password must be the password of the existing keystore.
# This script cannot be used to update the password of an existing keystore.

if [ -z "$DATAVAULT_HOME" ]; then
    echo "DATAVAULT_HOME is not set"
    exit 1
fi

if [ $# != 1 ]; then
    echo "Usage: "
    echo "./generate-keystore.sh <path_to_json_file>"
    echo "Example of json: "
cat <<- "EOF"
 {
  "path": "/path/to/keystore.ks",
  "password": "veryStrongPassword",
  "key_aliases": [
    "data-encryption-key",
    "ssh-encryption-key"
  ]
}
EOF
    exit 1
fi
JSON_FILE=$1
cd ${DATAVAULT_HOME}
# the broker jar has all the classes we need but we want a different 'main class' - org.datavaultplatform.common.crypto.Encryption
java -cp datavault-broker/target/datavault-broker.jar \
 -Dloader.main=org.datavaultplatform.common.crypto.Encryption \
 org.springframework.boot.loader.PropertiesLauncher generateSecretKeyAndAddToJCEKS "$JSON_FILE"

