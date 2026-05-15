#!/bin/bash

# 1. Get the directory where this script is stored
SCRIPT_DIR=$(cd -- "$(dirname -- "$0")" && pwd)

# 2. Change the working directory to that location
cd "$SCRIPT_DIR" || exit

TIMESTAMP=$(date +%Y%m%d_%H%M)
FILENAME="datavault_dv5demo_openapi_${TIMESTAMP}.zip"

# 1. Extract the base name (removes .zip extension if present)
DIRNAME="${FILENAME%.*}"

# 2. Create the temporary directory
mkdir -p "$DIRNAME"

# 3. Copy (or link) the files into that directory
cp datavault-broker-openapi.yaml datavault-webapp-openapi.yaml "$DIRNAME/"

# 4. Zip the directory
zip -r "$FILENAME" "$DIRNAME" -x "*.DS_Store"

# 5. Clean up the temporary directory
rm -rf "$DIRNAME"