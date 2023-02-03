#!/bin/bash

# This script takes a directory and tar's it up into a single tar file.
# example usage:
# $ ./tarDirectoryToTarFile.sh /directory/to/tar /tmp/output1.tar

RETVAL=1
if [ $# -eq 2 ] ; then
  STARTTIME=$(date +%s)
  SCRIPT_DIR=`dirname $0`
  ARG1="$1"
  ARG2="$2"
  echo "ARG1 $ARG1"
  echo "ARG1 $ARG2"
  java -cp $SCRIPT_DIR/datavault-worker.jar \
  -Dloader.main=org.datavaultplatform.worker.utils.TarBuilder \
  org.springframework.boot.loader.PropertiesLauncher "$ARG1" "$ARG2"
    RETVAL=$?
    ENDTIME=$(date +%s)
    DIFF=$(($ENDTIME - $STARTTIME))
    echo "It took $DIFF seconds to create the tar file"
    echo "exit status $RETVAL"
else
    echo "syntax: `basename $0` </directory/to/tar> </path/to/output.tar>"
fi