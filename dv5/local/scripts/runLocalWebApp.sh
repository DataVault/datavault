#!/bin/bash

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT
 SERVER_PORT=8888 \
 BROKER_TIMEOUT_MS=-1 \
 SPRING_PROFILES_ACTIVE=database \
 SPRING_SECURITY_DEBUG=false \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/webapp" \
 java -Xdebug \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5050 \
 -jar ./datavault-webapp/target/datavault-webapp.jar

