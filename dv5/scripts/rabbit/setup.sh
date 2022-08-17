#!/bin/bash
# sets up RABBIT_USER and checks to see if RABBIT_PASS has been passed in.
export RABBIT_USER=rabbit
if [ -z "$RABBIT_PASS" ]; then
      echo "RABBIT_PASS is empty, exiting.";
      exit 1;
fi;
#echo "using credentials [rabbit/$RABBIT_PASS]"