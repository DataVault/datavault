#! /bin/bash

set -e

/docker_datavault-home/scripts/wait-for-rabbitmq.sh

exec "$@"
