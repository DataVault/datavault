#! /bin/bash

set -e

${DATAVAULT_HOME}/scripts/wait-for-rabbitmq.sh
/usr/local/bin/ep ${DATAVAULT_HOME}/config/*

exec "$@"
