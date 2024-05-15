#!/bin/bash

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

# The 'local' profile for broker creates a LocalFileSystem in /tmp/as/local
mkdir -p /tmp/as/local

. $SCRIPT_DIR/setupBaseLoggingDirectory.sh

SSL_PASSWORD=thePassword

BROKER_P12_DIR=$(cd $SCRIPT_DIR/../; pwd)
BROKER_P12=$BROKER_P12_DIR/broker.p12

if [[ ! -f $BROKER_P12 ]]; then
  echo "ERROR: Broker PKCS#12 file not found: [$BROKER_P12]"
  exit 1
fi
echo "BROKER_P12 is [$BROKER_P12]"

SSL_SETTINGS=\
"SERVER_PORT=8443 \
 SERVER_SSL_ENABLED=true \
 SERVER_SSL_KEY_STORE_TYPE=PKCS12 \
 SERVER_SSL_KEY_STORE_PASSWORD=${SSL_PASSWORD} \
 SERVER_SSL_KEY_STORE=${BROKER_P12} \
 SERVER_SSL_KEY_ALIAS=ssl-key-dv-broker \
 SERVER_SSL_KEY_PASSWORD=${SSL_PASSWORD}"

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/broker.log"

#ENCRYPTION_SETTINGS=\
#"KEYSTORE_ENABLE=true \
# KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
# KEYSTORE_SHA1=da39a3ee5e6b4b0d3255bfef95601890afd80709 \
# VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
# KEYSTORE_PASSWORD=XXX-PASSWORD \
# VALIDATE_ENCRYPTION_CONFIG=true"

SETUP_ENV=\
"SPRING_PROFILES_ACTIVE=local \
 SPRING_SECURITY_DEBUG=true \
 SPRING_JPA_HIBERNATE_DDL_AUTO=update \
 SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION=true \
 SPRING_SQL_INIT_MODE=always \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-ssl/props/broker" \
 RABBITMQ_DEFINE_QUEUE_WORKER=true \
 RABBITMQ_DEFINE_QUEUE_BROKER=true \
 $LOGFILE_SETTINGS \
 $ENCRYPTION_SETTINGS \
 $SSL_SETTINGS"

JAVA_TOOL_OPTS=\
"-Duser.language=en \
 -Duser.country=GB \
 -Duser.home=$HOME \
 -Duser.dir=$SCRIPT_DIR \
 -Duser.timezone=Europe/London \
 -Xdebug \
 -Xms1024M -Xmx2024M \
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

 eval $SETUP_ENV \
 java $JAVA_TOOL_OPTS -jar $PROJECT_ROOT/datavault-broker/target/datavault-broker.jar

