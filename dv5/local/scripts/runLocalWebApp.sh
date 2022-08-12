#!/bin/bash

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

. ./setupBaseLoggingDirectory.sh

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT

LOGFILE_SETTINGS=\
"LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE=10KB \
 LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY=366 \
 LOGGING_FILE_NAME=$BASE_LOG_DIR/webapp.log"

SETUP_ENV=\
"SERVER_PORT=8888 \
 BROKER_TIMEOUT_MS=-1 \
 SPRING_PROFILES_ACTIVE=database \
 SPRING_SECURITY_DEBUG=false \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/webapp" \
 $LOGFILE_SETTINGS"

 eval $SETUP_ENV \
 java -Xdebug \
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5050 \
 -jar ./datavault-webapp/target/datavault-webapp.jar

