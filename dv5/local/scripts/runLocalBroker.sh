#!/bin/bash

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

# The 'local' profile for broker creates a LocalFileSystem in /tmp/as/local
mkdir -p /tmp/as/local

. ./setupBaseLoggingDirectory.sh

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/broker.log"

#ENCRYPTION_SETTINGS=\
#"KEYSTORE_ENABLE=true \
# KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
# VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
# KEYSTORE_PASSWORD=XXX-PASSWORD \
# VALIDATE_ENCRYPTION_CONFIG=true"

SETUP_ENV=\
"SERVER_PORT=8080 \
 SPRING_PROFILES_ACTIVE=local \
 SPRING_SECURITY_DEBUG=true \
 SPRING_JPA_HIBERNATE_DDL_AUTO=update \
 SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION=true \
 SPRING_SQL_INIT_MODE=always \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/broker" \
 RABBITMQ_DEFINE_QUEUE_WORKER=true \
 RABBITMQ_DEFINE_QUEUE_BROKER=true \
 $LOGFILE_SETTINGS \
 $ENCRYPTION_SETTINGS"

 eval $SETUP_ENV \
 java -Xdebug \
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 \
 -jar ./datavault-broker/target/datavault-broker.jar


