#!/bin/bash

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

# The 'local' profile for broker creates a LocalFileSystem in /tmp/as/local
mkdir -p /tmp/as/local

. $SCRIPT_DIR/setupBaseLoggingDirectory.sh

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/broker.log"

#ENCRYPTION_SETTINGS=\
#"KEYSTORE_ENABLE=true \
# KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
# KEYSTORE_SHA1=4499a0663669cc06e7099632ba360c15a404943c \
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
 LDAP_CONNECTION_TEST_SEARCH_TERM=Bond \
 $LOGFILE_SETTINGS \
 $ENCRYPTION_SETTINGS"

JAVA_TOOL_OPTS=\
"-Dcom.sun.management.jmxremote=true
 -Dcom.sun.management.jmxremote.port=6661
 -Dcom.sun.management.jmxremote.authenticate=false
 -Dcom.sun.management.jmxremote.ssl=false
 -Duser.language=en \
 -Duser.country=GB \
 -Duser.home=$PROJECT_ROOT/dv5/user/home \
 -Duser.dir=$PROJECT_ROOT/dv5/user/dir \
 -Duser.timezone=Europe/London \
 -Xdebug \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

 eval $SETUP_ENV \
 java $JAVA_TOOL_OPTS -jar $PROJECT_ROOT/datavault-broker/target/datavault-broker.jar

