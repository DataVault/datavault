#!/bin/bash

export PORT_WEBAPP=8888
export PORT_BROKER=8080
export PORT_WORKER=9090
export HOST=localhost
export CURL_OPTS=-s


which curl > /dev/null
if [ $? != 0 ]; then
  echo "This script requires 'curl'"
  exit 1;
fi

which jq > /dev/null
if [ $? != 0 ]; then
  echo "This script requires 'jq'"
  exit 1;
fi
