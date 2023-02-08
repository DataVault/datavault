#!/bin/bash

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

APP_NAME=worker-1

. $SCRIPT_DIR/setupBaseLoggingDirectory.sh

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/$APP_NAME.log"

#ENCRYPTION_SETTINGS=\
#"KEYSTORE_ENABLE=true \
# KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
# KEYSTORE_SHA1=4499a0663669cc06e7099632ba360c15a404943c \
# VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
# VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
# KEYSTORE_PASSWORD=XXX-PASSWORD \
# VALIDATE_ENCRYPTION_CONFIG=true"

SETUP_ENV=\
"SERVER_PORT=9090 \
 SPRING_APPLICATION_NAME=datavault-$APP_NAME \
 SPRING_SECURITY_DEBUG=true \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/worker" \
 RABBITMQ_DEFINE_QUEUE_WORKER=true \
 RABBITMQ_DEFINE_QUEUE_BROKER=true \
 CHECK_TSM_TAPE_DRIVER=true \
 CHECK_ORACLE_CLOUD_CONFIG=true \
 $LOGFILE_SETTINGS \
 $ENCRYPTION_SETTINGS"

JAVA_TOOL_OPTS=\
"-Dcom.sun.management.jmxremote=true
 -Dcom.sun.management.jmxremote.port=6663
 -Dcom.sun.management.jmxremote.authenticate=false
 -Dcom.sun.management.jmxremote.ssl=false
 -Duser.language=en \
 -Duser.country=GB \
 -Duser.home=$PROJECT_ROOT/dv5/user/home \
 -Duser.dir=$PROJECT_ROOT/dv5/user/dir \
 -Duser.timezone=Europe/London \
 -Xdebug \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5555"

 eval $SETUP_ENV \
 java $JAVA_TOOL_OPTS -jar $PROJECT_ROOT/datavault-worker/target/datavault-worker.jar

