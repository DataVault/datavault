package org.datavaultplatform.broker.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ActiveProfiles("test")
@TestPropertySource("classpath:datavault-test.properties")
@TestPropertySource(properties = {"mail.password=dummy","sftp.passphrase=DUMMY"})
public @interface AddTestProperties {

}
