#!/bin/bash

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

APP_NAME=worker-1

. $SCRIPT_DIR/setupBaseLoggingDirectory.sh

SSL_PASSWORD=thePassword

WORKER_P12_DIR=$(cd $SCRIPT_DIR/../; pwd)
WORKER_P12=$WORKER_P12_DIR/worker.p12

if [[ ! -f $WORKER_P12 ]]; then
  echo "ERROR: Worker PKCS#12 file not found: [$WORKER_P12]"
  exit 1
fi
echo "WORKER_P12 is [$WORKER_P12]"

SSL_SETTINGS=\
"SERVER_PORT=9443 \
 SERVER_SSL_ENABLED=true \
 SERVER_SSL_KEY_STORE_TYPE=PKCS12 \
 SERVER_SSL_KEY_STORE_PASSWORD=${SSL_PASSWORD} \
 SERVER_SSL_KEY_STORE=${WORKER_P12} \
 SERVER_SSL_KEY_ALIAS=ssl-key-dv-worker \
 SERVER_SSL_KEY_PASSWORD=${SSL_PASSWORD}"

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/$APP_NAME.log"

#ENCRYPTION_SETTINGS=\
#"KEYSTORE_ENABLE=true \
# KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
# KEYSTORE_SHA1=da39a3ee5e6b4b0d3255bfef95601890afd80709 \
# VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
# VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
# KEYSTORE_PASSWORD=XXX-PASSWORD \
# VALIDATE_ENCRYPTION_CONFIG=true"

SETUP_ENV=\
"SPRING_APPLICATION_NAME=datavault-$APP_NAME \
 SPRING_SECURITY_DEBUG=true \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-ssl/props/worker" \
 RABBITMQ_DEFINE_QUEUE_WORKER=true \
 RABBITMQ_DEFINE_QUEUE_BROKER=true \
 CHECK_TSM_TAPE_DRIVER=true \
 CHECK_ORACLE_CLOUD_CONFIG=true \
 $LOGFILE_SETTINGS \
 $ENCRYPTION_SETTINGS \
 $SSL_SETTINGS"

JAVA_TOOL_OPTS=\
"-Duser.language=en \
 -Duser.country=GB \
 -Duser.home=$HOME \
 -Duser.dir=$SCRIPT_DIR \
 -Duser.timezone=Europe/London -Djava.net.preferIPv4Stack=true \
 -Xdebug \
 -Xms1024M -Xmx2024M \
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5555"

 eval $SETUP_ENV \
 java $JAVA_TOOL_OPTS -jar $PROJECT_ROOT/datavault-worker/target/datavault-worker.jar

