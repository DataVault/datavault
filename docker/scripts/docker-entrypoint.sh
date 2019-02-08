#! /bin/bash

set -e

/usr/local/bin/ep ${DATAVAULT_HOME}/scripts/set-defaults.sh
source ${DATAVAULT_HOME}/scripts/set-defaults.sh

/usr/local/bin/ep ${DATAVAULT_HOME}/config/*
/usr/local/bin/ep ${DATAVAULT_HOME}/scripts/fix-permissions.sh

${DATAVAULT_HOME}/scripts/fix-permissions.sh

if [ $1 == "broker" ] || [ $1 == "worker" ]; then
	/usr/local/bin/wait-for-it ${RABBITMQ_HOST}:15672 --timeout=90 --strict -- echo "RabbitMQ is ready"
	if [ $1 == "broker" ]; then
		/usr/local/bin/wait-for-it ${MYSQL_HOST}:3306 --timeout=90 --strict -- echo "MySQL is ready"
	fi
fi
shift

cd ${DATAVAULT_HOME}/lib

su-exec datavault "$@"
