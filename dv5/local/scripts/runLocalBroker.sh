#!/bin/bash

java -version

mkdir -p /tmp/as/local

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT
 SPRING_PROFILES_ACTIVE=local \
 SPRING_SECURITY_DEBUG=true \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/broker" \
 ./mvnw spring-boot:run  \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" \
 --projects datavault-broker

