#!/bin/bash
PASS=thePassword;
keytool -list -v -storepass $PASS -keystore broker.p12;
keytool -list -v -storepass $PASS -keystore webapp.p12;
keytool -list -v -storepass $PASS -keystore worker.p12;
