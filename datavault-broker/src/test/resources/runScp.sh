#!/bin/sh
## this script is meant to be run in a testcontainer
## it is used to test ssh keypair using scp
## see org.datavaultplatform.broker.services.UserKeyPairService2IT
scp \
 -o UserKnownHostsFile=/dev/null \
 -o StrictHostKeyChecking=no \
 -P 2222 \
 -i /tmp/test_rsa \
 /tmp/randFrom.txt \
 testuser@testto:/tmp/randTo.txt


