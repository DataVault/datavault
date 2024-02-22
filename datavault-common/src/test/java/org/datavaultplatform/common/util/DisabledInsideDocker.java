package org.datavaultplatform.common.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.condition.DisabledIf;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@DisabledIf("org.datavaultplatform.common.util.DockerUtils#isRunningInsideDocker")
public @interface DisabledInsideDocker {
}
