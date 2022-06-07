#!/bin/bash

java -version

export PROJECT_ROOT=$(cd ../../;pwd)
cd $PROJECT_ROOT

./mvnw clean install -Powasp-dependency-check

date; find . -name dependency-check-report.html -ls