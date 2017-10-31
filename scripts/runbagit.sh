#!/bin/bash

if [ $# -eq 1 ] ; then
    ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"/..
    VERSION=`grep "<version" $ROOT_DIR/pom.xml | head -1 | sed -e 's/<\/\?version>//g' | awk '{$1=$1;print}' `
    LIB_DIR="$ROOT_DIR/datavault-assembly/target/datavault-assembly-$VERSION-assembly/datavault-home/lib"
    java -cp "$LIB_DIR/*" -Dlog4j.configuration=$ROOT_DIR/datavault-worker/src/test/resources/log4j.properties org.datavaultplatform.worker.operations.Packager $@
    exit $?
else
    echo "syntax: `basename $0` <datadir>"
    exit 1
fi
