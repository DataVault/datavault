#!/bin/bash

# This script decrypts a set of encrypted tar file chunks and re-combine them back into a single tar file.
# example usage:
# $ /path/to/datavault/dv5/scripts/decryption/decryptChunkFiles.sh /path/to/decryptChunkParams.json

RETVAL=1
if [ $# -eq 1 ] ; then
  STARTTIME=$(date +%s)
  SCRIPT_DIR=`dirname $0`
  ROOT_DIR=$(cd $SCRIPT_DIR/../../../;pwd)
  cd $ROOT_DIR
  ARGS="$@"
  echo "ARGS $ARGS"
  java -cp datavault-worker/target/datavault-worker.jar \
  -Dloader.main=org.datavaultplatform.worker.utils.DecryptedTarBuilder \
  org.springframework.boot.loader.PropertiesLauncher "$ARGS"
    RETVAL=$?
    ENDTIME=$(date +%s)
    DIFF=$(($ENDTIME - $STARTTIME))
    echo "It took $DIFF seconds to decrypt the chunks"
    echo "exit status $RETVAL"
else
    echo "syntax: `basename $0` <decryptChunkParams.json>"
fi