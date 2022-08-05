#!/bin/bash

# This script now uses 'java -jar' instead of 'mvnw spring-boot:run'
java -version

# The 'local' profile for broker creates a LocalFileSystem in /tmp/as/local
mkdir -p /tmp/as/local

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT
 SERVER_PORT=8080 \
 SPRING_PROFILES_ACTIVE=local \
 SPRING_SECURITY_DEBUG=true \
 SPRING_JPA_HIBERNATE_DDL_AUTO=update \
 SPRING_JPA_DEFER_DATASOURCE_INITIALIZATION=true \
 SPRING_SQL_INIT_MODE=always \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local/props/broker" \
#KEYSTORE_ENABLE=true \
#KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
#VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
#VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
#KEYSTORE_PASSWORD=XXX-PASSWORD \
 VALIDATE_ENCRYPTION_CONFIG=true \
 BROKER_DEFINE_QUEUE_WORKER=true \
 BROKER_DEFINE_QUEUE_BROKER=true \
 java -Xdebug \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 \
 -jar ./datavault-broker/target/datavault-broker.jar


