#!/bin/bash

java -version

mkdir -p /tmp/as/local

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT
 SPRING_PROFILES_ACTIVE=local \
 SPRING_SECURITY_DEBUG=true \
 SPRING_JPA_HIBERNATE_DDL_AUTO=update \
 SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION=true \
 SPRING_SQL_INIT_MODE=always \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/broker" \
#KEYSTORE_ENABLE=true \
#KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
#VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME \
#KEYSTORE_PASSWORD=XXX-PASSWORD \
 VALIDATE_ENCRYPTION_CONFIG=false \
 ./mvnw spring-boot:run  \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" \
 --projects datavault-broker

