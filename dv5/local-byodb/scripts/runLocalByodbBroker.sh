#!/bin/bash

java -version

# The 'local' profile for broker creates a LocalFileSystem in /tmp/as/local
mkdir -p /tmp/as/local

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

cd $PROJECT_ROOT
 SERVER_PORT=8080 \
 SPRING_PROFILES_ACTIVE=local \
 SPRING_SECURITY_DEBUG=true \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-byodb/props/broker" \
 SPRING_JPA_HIBERNATE_DDL_AUTO=update \
 SPRING_SQL_INIT_MODE=never \
 SPRING_DATASOURCE_USERNAME=root \
 SPRING_DATASOURCE_PASSWORD=rootpass \
 SPRING_DATASOURCE_URL='jdbc:mariadb://localhost:53306/datavault?useSSL=false&serverTimezone=UTC' \
#KEYSTORE_ENABLE=true \
#KEYSTORE_PATH=XXX/PATH/TO/KEYSTORE.ks \
#KEYSTORE_SHA1=4499a0663669cc06e7099632ba360c15a404943c \
#VAULT_PRIVATEKEYENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-PRIVATE-KEYS \
#VAULT_DATAENCRYPTIONKEYNAME=XXX-KEYNAME-FOR-DATA-KEYS \
#KEYSTORE_PASSWORD=XXX-PASSWORD \
 VALIDATE_ENCRYPTION_CONFIG=true \
 RABBITMQ_DEFINE_QUEUE_WORKER=true \
 RABBITMQ_DEFINE_QUEUE_BROKER=true \
 LDAP_CONNECTION_TEST_SEARCH_TERM=Bond \
 ./mvnw spring-boot:run  \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xms1024M -Xmx2024M \
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London \
 -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" \
 --projects datavault-broker

