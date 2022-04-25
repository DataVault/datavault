#!/bin/bash

java -version

#this script runs the datavault webapp in 'standalone' mode - this helps test Spring and SpringSecurity config.
#this script uses Maven with Spring Boot specific goal
export PROJECT_ROOT=$(cd ../../;pwd)
cd $PROJECT_ROOT
DATAVAULT_HOME="$PROJECT_ROOT/dv5" SPRING_PROFILES_ACTIVE=standalone ./mvnw spring-boot:run --projects datavault-webapp

