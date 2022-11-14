#!/bin/bash

BASE_DIR=/tmp/dv
echo "-----------------------------"
echo "-  Deposit Test : Source   -"
echo "-----------------------------"
tree -C -D -t -s $BASE_DIR/src
echo "-----------------------------"
echo "-   Deposit Test : Dest     -"
echo "-----------------------------"
tree -C -D -t -s $BASE_DIR/dest