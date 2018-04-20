#! /bin/bash

set -e

/docker_datavault-home/scripts/wait-for-rabbitmq.sh
/usr/local/bin/ep -v /docker_datavault-home/config/*

exec "$@"
