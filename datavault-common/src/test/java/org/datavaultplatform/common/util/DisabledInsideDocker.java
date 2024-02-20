package org.datavaultplatform.common.util;

import org.junit.jupiter.api.condition.DisabledIf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@DisabledIf("org.datavaultplatform.common.util.DockerUtils#isRunningInsideDocker")
public @interface DisabledInsideDocker {
}
