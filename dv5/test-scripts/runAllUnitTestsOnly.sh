#!/bin/bash

java -version

export PROJECT_ROOT=$(cd ../../;pwd)
cd $PROJECT_ROOT

./mvnw clean test