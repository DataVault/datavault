#!/bin/bash

java -version

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT
 SPRING_PROFILES_ACTIVE=database \
 SPRING_SECURITY_DEBUG=false \
 SERVER_PORT=8888 \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/webapp" \
 ./mvnw spring-boot:run  \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5050" \
 --projects datavault-webapp

