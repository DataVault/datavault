#!/bin/bash

SCRIPT_DIR=`dirname $0`
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

# The 'local' profile for broker creates a LocalFileSystem in /tmp/as/local
mkdir -p /tmp/as/local

. $SCRIPT_DIR/setupBaseLoggingDirectory.sh

SSL_PASSWORD=thePassword

BROKER_P12=$(readlink -f $SCRIPT_DIR/../broker.p12)

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
 -Duser.home=$PROJECT_ROOT \
 -Duser.timezone=Europe/London \
 -Xdebug \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

 eval $SETUP_ENV \
 java $JAVA_TOOL_OPTS -jar $PROJECT_ROOT/datavault-broker/target/datavault-broker.jar

