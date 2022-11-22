#!/bin/bash

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

. $SCRIPT_DIR/setupBaseLoggingDirectory.sh

SSL_PASSWORD=thePassword

WEBAPP_P12_DIR=$(cd $SCRIPT_DIR/../; pwd)
WEBAPP_P12=$WEBAPP_P12_DIR/webapp.p12

if [[ ! -f $WEBAPP_P12 ]]; then
  echo "ERROR: Webapp PKCS#12 file not found: [$WEBAPP_P12]"
  exit 1
fi
echo "WEBAPP_P12 is [$WEBAPP_P12]"

SSL_SETTINGS=\
"BROKER_URL=https://broker.dv.local:8443 \
 BROKER_USING_SELFSIGNEDCERT=true \
 SERVER_PORT=7443 \
 SERVER_SSL_ENABLED=true \
 SERVER_SSL_KEY_STORE_TYPE=PKCS12 \
 SERVER_SSL_KEY_STORE_PASSWORD=${SSL_PASSWORD} \
 SERVER_SSL_KEY_STORE=${WEBAPP_P12} \
 SERVER_SSL_KEY_ALIAS=ssl-key-dv-webapp \
 SERVER_SSL_KEY_PASSWORD=${SSL_PASSWORD}"

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/webapp.log"

SETUP_ENV=\
"BROKER_TIMEOUT_MS=-1 \
 SPRING_PROFILES_ACTIVE=database \
 SPRING_SECURITY_DEBUG=false \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-ssl/props/webapp" \
 $LOGFILE_SETTINGS \
 $SSL_SETTINGS"

JAVA_TOOL_OPTS=\
"-Duser.language=en \
 -Duser.country=GB \
 -Duser.home=$PROJECT_ROOT \
 -Duser.timezone=Europe/London \
 -Xdebug \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5050"

 eval $SETUP_ENV \
 java $JAVA_TOOL_OPTS -jar $PROJECT_ROOT/datavault-webapp/target/datavault-webapp.jar

