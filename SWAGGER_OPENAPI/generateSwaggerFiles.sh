#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)

# Move to the parent of that directory
PARENT_DIR="$SCRIPT_DIR/.."

cd "$PARENT_DIR" || { 
    echo "Error: Could not change directory to $PARENT_DIR" >&2
    exit 1 
}

# Optional: Print it to verify
# echo "Current working directory is now: $(pwd)"

# echo BEFORE
# ls -l SWAGGER_OPENAPI/*.yaml

./mvnw -pl datavault-webapp test -Dtest=OpenApiWebAppTest#testOpenApiAsYaml -Dgenerate.swagger.doc=true
./mvnw -pl datavault-broker test -Dtest=OpenApiBrokerTest#testOpenApiAsYaml -Dgenerate.swagger.doc=true

echo AFTER
ls -l SWAGGER_OPENAPI/*.yaml





