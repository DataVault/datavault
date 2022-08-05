#!/bin/bash

java -version

# The 'local' profile for broker creates a LocalFileSystem in /tmp/as/local
mkdir -p /tmp/as/local

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT
 SERVER_PORT=8080 \
 SPRING_PROFILES_ACTIVE=local \
 SPRING_SECURITY_DEBUG=true \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-byodb/props/broker" \
 SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
 SPRING_SQL_INIT_MODE=never \
 SPRING_DATASOURCE_USERNAME=user \
 SPRING_DATASOURCE_PASSWORD=userpass \
 SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC' \
#KEYSTORE_ENABLE=true \
#KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
#VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
#VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
#KEYSTORE_PASSWORD=XXX-PASSWORD \
 VALIDATE_ENCRYPTION_CONFIG=true \
 BROKER_DEFINE_QUEUE_WORKER=true \
 BROKER_DEFINE_QUEUE_BROKER=true \
 ./mvnw spring-boot:run  \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" \
 --projects datavault-broker

