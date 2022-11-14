package org.datavaultplatform.webapp.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.TestPropertySource;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TestPropertySource("classpath:datavault-test.properties")
public @interface AddTestProperties {

}
