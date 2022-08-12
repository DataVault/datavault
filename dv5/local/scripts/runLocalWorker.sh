#!/bin/bash

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

APP_NAME=datavault-worker-1

. ./setupBaseLoggingDirectory.sh

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/$APP_NAME.log"

#ENCRYPTION_SETTINGS=\
#"KEYSTORE_ENABLE=true \
# KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
# VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
# VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
# KEYSTORE_PASSWORD=XXX-PASSWORD \
# VALIDATE_ENCRYPTION_CONFIG=true"

SETUP_ENV=\
"SERVER_PORT=9090 \
 SPRING_APPLICATION_NAME=$APP_NAME \
 SPRING_SECURITY_DEBUG=true \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/worker" \
 RABBITMQ_DEFINE_QUEUE_WORKER=true \
 RABBITMQ_DEFINE_QUEUE_BROKER=true \
 $LOGFILE_SETTINGS \
 $ENCRYPTION_SETTINGS"

 eval $SETUP_ENV \
 java -Xdebug \
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5555 \
 -jar ./datavault-worker/target/datavault-worker.jar

