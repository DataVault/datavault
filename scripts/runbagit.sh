#!/bin/bash

# This script creates a bagit bag for a given directory and prints out the number of seconds it took.
# exmaple usage:
# $ </path/to/datavault/scripts/runbagit.sh /path/to/dir/to/bag/>

RETVAL=1
if [ $# -eq 1 ] ; then
    STARTTIME=$(date +%s)
    ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/..
    VERSION=`grep "<version" $ROOT_DIR/pom.xml | head -1 | sed -e 's/<\/\?version>//g' | awk '{$1=$1;print}' `
    LIB_DIR="$ROOT_DIR/datavault-assembly/target/datavault-assembly-$VERSION-assembly/datavault-home/lib"
    java -cp "$LIB_DIR/*" -Dlog4j.configuration=$ROOT_DIR/datavault-worker/src/test/resources/log4j.properties org.datavaultplatform.worker.operations.Packager $@
    RETVAL=$?
    ENDTIME=$(date +%s)
    echo "It took $(($ENDTIME - $STARTTIME)) seconds to complete this task..."
else
    echo "syntax: `basename $0` <datadir>"
fi

exit $RETVAL
