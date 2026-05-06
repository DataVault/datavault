#!/bin/bash

java -version

SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
PROJECT_ROOT=$(cd $SCRIPT_DIR/../../..;pwd)

cd $PROJECT_ROOT
 SERVER_PORT=8888 \
 SPRING_PROFILES_ACTIVE=database \
 LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=DEBUG \
 SPRING_SECURITY_DEBUG=true \
 LDAP_CONNECTION_TEST_SEARCH_TERM=Bond \
 DATAVAULT_HOME="$PROJECT_ROOT/dv5/local-byodb/props/webapp" \
 LOGGING_PATTERN_CONSOLE='%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %clr([trace=%X{traceId:-} span=%X{spanId:-} user=%X{user:-}]){yellow} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%rEx}' \
 ./mvnw spring-boot:run  \
 -Dspring-boot.run.jvmArguments="-Xdebug \
 -Xms1024M -Xmx2024M \
 -Duser.language=en -Duser.country=GB -Duser.timezone=Europe/London -Djava.net.preferIPv4Stack=true \
 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5050" \
 --projects datavault-webapp

