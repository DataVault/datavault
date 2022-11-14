#!/bin/bash

if [ -z "$DATAVAULT_HOME" ]; then
    echo "DATAVAULT_HOME is not set"
    exit 1
fi

cd ${DATAVAULT_HOME}
# the broker jar has all the classes we need but we want a different 'main class' - org.datavaultplatform.common.crypto.Encryption
OUTPUT=$(java -cp datavault-broker/target/datavault-broker.jar \
 -Dloader.main=org.datavaultplatform.common.crypto.Encryption \
 org.springframework.boot.loader.PropertiesLauncher generateSecretKey)
STATUS=$?
if [ $STATUS -ne 0 ]; then
  echo "ERROR : "
  echo "---------------------"
  echo $OUTPUT
  echo "---------------------"
else
  KEY=$(echo "$OUTPUT" | tail -1)
  echo "SUCCESS : KEY IS ..."
  echo "---------------------"
  echo $KEY
  echo "---------------------"
fi
