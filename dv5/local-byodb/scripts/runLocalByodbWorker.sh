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
#KEYSTORE_SHA1=4499a0663669cc06e7099632ba360c15a404943c \
#VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
#VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
#KEYSTORE_PASSWORD=XXX-PASSWORD \
 VALIDATE_ENCRYPTION_CONFIG=true \
 RABBITMQ_DEFINE_QUEUE_WORKER=true \
 RABBITMQ_DEFINE_QUEUE_BROKER=true \
 CHECK_TSM_TAPE_DRIVER=true \
 CHECK_ORACLE_CLOUD_CONFIG=true \
 CHUNKING_SIZE=20MB \
 VALIDATE_WORKER_DIRS=true \
 ./mvnw spring-boot:run \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xms1024M -Xmx2024M \
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5555" \
 --projects datavault-worker

