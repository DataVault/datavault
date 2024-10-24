package org.datavaultplatform.common.timeout;


/*
 This test can be run with the following from the command line to test junit timeouts.
 1) ./mvnw clean test -pl datavault-common -Dtest="TimeoutTest" 
    - uses default timeout see pom.xml / maven-surefire-plugin
 2) ./mvnw clean test -pl datavault-common -Dtest="TimeoutTest"  -Djunit.jupiter.execution.timeout.default='5 s'
    - uses timeout specified in command line
 */
public class TimeoutTest extends BaseTimeoutTest {

    public TimeoutTest() {
        super(1, 10);
    }
}
