#!/bin/bash

java -version

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

cd $PROJECT_ROOT
 SERVER_PORT=8888 \
 SPRING_PROFILES_ACTIVE=database \
 SPRING_SECURITY_DEBUG=false \
 LDAP_CONNECTION_TEST_SEARCH_TERM=Bond \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-byodb/props/webapp" \
 ./mvnw spring-boot:run  \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xms1024M -Xmx2024M \
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London \
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5050" \
 --projects datavault-webapp

# NOTE for java 8  - address=5050
# NOTE for java 9+ - address=*:5050

