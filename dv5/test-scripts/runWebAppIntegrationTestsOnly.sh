#!/bin/bash

java -version

export PROJECT_ROOT=$(cd ../../;pwd)
cd $PROJECT_ROOT

./mvnw clean integration-test -Dskip.unit.tests -pl datavault-webapp
