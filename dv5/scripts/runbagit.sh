#!/bin/bash

# This script creates a bagit bag for a given directory and prints out the number of seconds it took.
# exmaple usage:
# $ </path/to/datavault/dv5/scripts/runbagit.sh /path/to/dir/to/bag/>

RETVAL=1
if [ $# -eq 1 ] ; then
  STARTTIME=$(date +%s)
  SCRIPT_DIR=`dirname $0`
  ROOT_DIR=$(cd $SCRIPT_DIR/../..;pwd)
  cd $ROOT_DIR
  ARGS="$@"
  java -cp datavault-worker/target/datavault-worker.jar \
  -Dloader.main=org.datavaultplatform.worker.operations.PackagerV2 \
  org.springframework.boot.loader.PropertiesLauncher "$ARGS"
    RETVAL=$?
    ENDTIME=$(date +%s)
    echo "It took $(($ENDTIME - $STARTTIME)) seconds to complete this task..."
else
    echo "syntax: `basename $0` <datadir>"
fi

exit $RETVAL
