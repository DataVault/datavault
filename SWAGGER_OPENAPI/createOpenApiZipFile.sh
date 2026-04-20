#!/bin/bash

# 1. Get the directory where this script is stored
SCRIPT_DIR=$(cd -- "$(dirname -- "$0")" && pwd)

# 2. Change the working directory to that location
cd "$SCRIPT_DIR" || exit

TIMESTAMP=$(date +%Y%m%d_%H%M)
FILENAME="datavault_dv5demo_openapi_${TIMESTAMP}.zip"

zip $FILENAME datavault-broker-openapi.yaml datavault-webapp-openapi.yaml -x "*.DS_Store"