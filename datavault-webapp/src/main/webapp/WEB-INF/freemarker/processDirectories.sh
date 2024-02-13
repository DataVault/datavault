#!/usr/bin/env bash
cd "$(dirname "$0")"
echo "cwd is $PWD"
find $PWD -type d -exec ./processDirectory.sh {} \;