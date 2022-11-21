#!/bin/bash

export PORT_WEBAPP=7443
export PORT_BROKER=8443
export PORT_WORKER=9443
export BROKER_HOST=broker.dv.local
export WEBAPP_HOST=webapp.dv.local
export WORKER_HOST=worker.dv.local
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
