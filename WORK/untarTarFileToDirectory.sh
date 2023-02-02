#!/bin/bash

# This script takes a tar file and extracts it to the supplied directory
# example usage:
# $ ./untarTarFileToDirectory.sh /tmp/input1.tar /target/untar/dir/

RETVAL=1
if [ $# -eq 2 ] ; then
  STARTTIME=$(date +%s)
  SCRIPT_DIR=`dirname $0`
  ARG1="$1"
  ARG2="$2"
  echo "ARG1 $ARG1"
  echo "ARG1 $ARG2"
  java -cp $SCRIPT_DIR/datavault-worker.jar \
  -Dloader.main=org.datavaultplatform.worker.utils.UnTarBuilder \
  org.springframework.boot.loader.PropertiesLauncher "$ARG1" "$ARG2"
    RETVAL=$?
    ENDTIME=$(date +%s)
    DIFF=$(($ENDTIME - $STARTTIME))
    echo "It took $DIFF seconds to un-tar the tar file"
    echo "exit status $RETVAL"
else
    echo "syntax: `basename $0` </tmp/input1.tar> </target/untar/dir/>"
fi