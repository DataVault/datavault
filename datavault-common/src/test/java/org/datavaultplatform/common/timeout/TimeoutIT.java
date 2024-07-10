package org.datavaultplatform.common.timeout;


/*
 This test can be run with the following from the command line to test junit timeouts.
 1) ./mvnw clean verify -pl datavault-common -Dskip.unit.tests=true -Dit.test="TimeoutIT" 
    - uses default timeout see pom.xml / maven-failsafe-plugin
 2) ./mvnw clean verify -pl datavault-common -Dskip.unit.tests=true -Dit.test="TimeoutIT" -pl datavault-common -Djunit.jupiter.execution.timeout.default='5 s'
    - uses timeout specified in command line
 */
public class TimeoutIT extends BaseTimeoutTest {

    public TimeoutIT() {
        super(1, 10);
    }
}
