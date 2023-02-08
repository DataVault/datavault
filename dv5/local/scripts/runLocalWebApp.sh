#!/bin/bash

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

. $SCRIPT_DIR/setupBaseLoggingDirectory.sh

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
 LDAP_CONNECTION_TEST_SEARCH_TERM=Bond \
 $LOGFILE_SETTINGS"

JAVA_TOOL_OPTS=\
"-Dcom.sun.management.jmxremote=true
 -Dcom.sun.management.jmxremote.port=6662
 -Dcom.sun.management.jmxremote.authenticate=false
 -Dcom.sun.management.jmxremote.ssl=false
 -Duser.language=en \
 -Duser.country=GB \
 -Duser.home=$PROJECT_ROOT/dv5/user/home \
 -Duser.dir=$PROJECT_ROOT/dv5/user/dir \
 -Duser.timezone=Europe/London \
 -Xdebug \
 -Xms1024M -Xmx2024M \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5050"

 eval $SETUP_ENV \
 java $JAVA_TOOL_OPTS -jar $PROJECT_ROOT/datavault-webapp/target/datavault-webapp.jar

