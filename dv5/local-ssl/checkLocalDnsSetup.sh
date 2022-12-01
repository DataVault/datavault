#!/bin/bash

ping -q -c 1 worker.dv.local >/dev/null 2>&1
PING_WORKER=$?
if [[ $PING_WORKER -ne 0 ]]; then
    echo "worker.dv.local is NOT in /etc/hosts"
else
    echo "webapp.dv.local is in /etc/hosts"
fi

ping -q -c 1 broker.dv.local >/dev/null 2>&1
PING_BROKER=$?
if [[ $PING_BROKER -ne 0 ]]; then
    echo "broker.dv.local is NOT in /etc/hosts"
else
    echo "broker.dv.local is in /etc/hosts"
fi

ping -q -c 1 webapp.dv.local >/dev/null 2>&1
PING_WEBAPP=$?
if [[ $PING_WEBAPP -ne 0 ]]; then
    echo "webapp.dv.local is NOT in /etc/hosts"
else
    echo "webapp.dv.local is in /etc/hosts"
fi
