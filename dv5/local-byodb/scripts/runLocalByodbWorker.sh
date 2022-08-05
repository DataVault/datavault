#!/bin/bash

java -version

export PROJECT_ROOT=$(cd ../../../;pwd)
cd $PROJECT_ROOT
 SERVER_PORT=9090 \
 SPRING_APPLICATION_NAME=datavault-worker-1 \
 SPRING_SECURITY_DEBUG=true \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-byodb/props/worker" \
#KEYSTORE_ENABLE=true \
#KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
#VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
#VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
#KEYSTORE_PASSWORD=XXX-PASSWORD \
 VALIDATE_ENCRYPTION_CONFIG=true \
 WORKER_DEFINE_QUEUE_WORKER=true \
 WORKER_DEFINE_QUEUE_BROKER=true \
 ./mvnw spring-boot:run \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5555" \
 --projects datavault-worker

