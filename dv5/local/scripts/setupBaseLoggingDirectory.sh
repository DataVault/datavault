#!/bin/bash

export BASE_LOG_DIR=/tmp/dv/log
mkdir -p $BASE_LOG_DIR
if [ ! -d $BASE_LOG_DIR ]; then
  echo "The log directory $BASE_LOG_DIR does not exist.";
  exit 1
fi
if [ ! -w $BASE_LOG_DIR ]; then
  echo "The log directory $BASE_LOG_DIR is not writable.";
  exit 1
fi
echo "The log directory $BASE_LOG_DIR exists and is writable."
