#!/bin/bash

java -version

#this script runs the datavault webapp in 'standalone' mode - this helps test Spring and SpringSecurity config.
#this script uses executable WAR file.
#to ensure that the war file exists!
export PROJECT_ROOT=$(cd ../../;pwd)
cd $PROJECT_ROOT
./mvnw -Dmaven.test.skip=true package
DATAVAULT_HOME="$PROJECT_ROOT/dv5" SPRING_PROFILES_ACTIVE=standalone java -jar datavault-webapp/target/datavault-webapp.war
