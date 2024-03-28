#!/bin/bash
# Generates HTML Files from 2 DataVault Projects for Comparison.
# Author :David Hay
# Date  : 28-Mar-2024
#
# This script generates pairs of html files using THIS DataVault Project and ANOTHER DataVault Project
# Each html file pair is generated using the template in THIS project and the template in the OTHER project.
BASE_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
#
# 
# NOTE  : ***You MUST set the value of the 'PROJECT_OTHER' value***
# NOTE  : ***You CAN  set the value of the 'LABEL_THIS' value***
# NOTE  : ***You CAN  set the value of the 'LABEL_OTHER' value***
#
# 
PROJECT_OTHER=/Users/davidhay/DEV/DV/datavault 
LABEL_THIS='tleaf'
LABEL_OTHER='fmarker'
PROJECT_THIS="$( cd -- "$(dirname "${BASE_DIR}/../../../")" >/dev/null 2>&1 ; pwd -P )"
if [[ "$PROJECT_OTHER" == "$PROJECT_THIS" ]]; then
  echo "The value of PROJECT_OTHER and PROJECT_THIS should not be the same"
  exit 1;
fi
TEMPLATES=$(realpath "${PROJECT_THIS}/TEMPLATES")
echo "OTHER PROJECT: $PROJECT_OTHER"
echo "OTHER LABEL: $LABEL_OTHER"
echo "THIS PROJECT: $PROJECT_THIS"
echo "THIS LABEL: $LABEL_THIS"
echo "TEMPLATES DIRECTORY IS ${TEMPLATES}"
rm -rf ${TEMPLATES}/*.html
java -version
#
cd $PROJECT_OTHER
export BRANCH_OTHER=$(git rev-parse --abbrev-ref HEAD)
echo "Generating '${LABEL_OTHER}' templates from $PROJECT_OTHER (branch $BRANCH_OTHER)"
./mvnw clean -Dskip.unit.tests=true -Dskip.integration.tests=true install > /dev/null
./mvnw \
 -DDV_LOCAL_TEST_TEMPLATE_OUTPUT_BASE_DIR=${TEMPLATES} \
 -DDV_LOCAL_TEST_TEMPLATE_OUTPUT_LABEL=${LABEL_OTHER} \
 -Dtest=ThymeleafTemplateTest \
 -pl datavault-webapp test > /dev/null
cd $BASE_DIR
echo "Generated '${LABEL_OTHER}' templates."
#
cd $PROJECT_THIS
export BRANCH_THIS=$(git rev-parse --abbrev-ref HEAD)
echo "Generating '${LABEL_THIS}' templates from $PROJECT_THIS (branch $BRANCH_THIS)"
./mvnw clean -Dskip.unit.tests=true -Dskip.integration.tests=true install > /dev/null
./mvnw \
 -DDV_LOCAL_TEST_TEMPLATE_OUTPUT_BASE_DIR=${TEMPLATES} \
 -DDV_LOCAL_TEST_TEMPLATE_OUTPUT_LABEL=${LABEL_THIS} \
 -Dtest=ThymeleafTemplateTest \
 -pl datavault-webapp test > /dev/null
cd $BASE_DIR
echo "Generated '${LABEL_THIS}' templates."
#
cd $TEMPLATES
echo "'old' and 'new' generated HTML files"
ls -1 *.html | xargs -n 2
cd $BASE_DIR
echo 'fin.'
