#! /bin/bash

set -e

/usr/local/bin/ep ${DATAVAULT_HOME}/scripts/set-defaults.sh
source ${DATAVAULT_HOME}/scripts/set-defaults.sh

/usr/local/bin/ep ${DATAVAULT_HOME}/config/*
/usr/local/bin/ep ${DATAVAULT_HOME}/scripts/fix-permissions.sh
/usr/local/bin/ep ${DATAVAULT_HOME}/scripts/wait-for-rabbitmq.sh

${DATAVAULT_HOME}/scripts/fix-permissions.sh
${DATAVAULT_HOME}/scripts/wait-for-rabbitmq.sh

su-exec datavault "$@"
