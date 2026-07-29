#!/bin/bash

java -version

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

mkdir -p /tmp/datavault/{temp,meta}

cd $PROJECT_ROOT

# Interactive prompt
SCRIPT_NAME=$(basename "$0")
ALT_SCRIPT_NAME="runLocalWebApp.sh"
echo "WARNING: You are running $SCRIPT_NAME"
echo "Did you mean to run $ALT_SCRIPT_NAME instead ?"
read -p "ARE YOU SURE YOU WANT TO RUN ${SCRIPT_NAME}? (y/N): " confirm

# Check if input is y or Y
if [[ ! "$confirm" =~ ^[yY]$ ]]; then
    echo "Exiting. Please run $ALT_SCRIPT_NAME instead."
    exit 1
fi

echo "Proceeding to run $SCRIPT_NAME"
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
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London -Djava.net.preferIPv4Stack=true \
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5555" \
 --projects datavault-worker

