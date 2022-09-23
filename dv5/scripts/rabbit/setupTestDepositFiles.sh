#!/bin/bash


BASE_DIR=/tmp/dv
SRC_DIR=$BASE_DIR/src/src-path-1
DEST_DIR=$BASE_DIR/dest
mkdir -p $SRC_DIR
mkdir -p $DEST_DIR
TEST_FILE=$SRC_DIR/hello.txt
if [[ ! -f $TEST_FILE ]]; then
  echo "hello" > /tmp/dv/src/src-path-1/hello.txt
fi
echo "Deposit Test : Source : $SRC_DIR"
echo "Deposit Test : Dest   : $DEST_DIR"


