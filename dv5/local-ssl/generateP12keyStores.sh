#!/bin/bash

PASS=thePassword;

# delete any existing p12 files
rm -f broker.p12 webapp.p12 worker.p12

# Generated broker.p12
keytool -genkeypair -keyalg RSA  \
 -keysize 4096 -validity 3650 -storeType PKCS12 \
 -keypass $PASS -storepass $PASS \
 -alias ssl-key-dv-broker -dname "CN=broker.dv.local" -keystore broker.p12

# Generated webapp.p12
keytool -genkeypair -keyalg RSA  \
 -keysize 4096 -validity 3650 -storeType PKCS12 \
 -keypass $PASS -storepass $PASS \
 -alias ssl-key-dv-webapp -dname "CN=webapp.dv.local" -keystore webapp.p12

# Generated workr.p12
keytool -genkeypair -keyalg RSA  \
 -keysize 4096 -validity 3650 -storeType PKCS12 \
 -keypass $PASS -storepass $PASS \
 -alias ssl-key-dv-worker -dname "CN=worker.dv.local" -keystore worker.p12

