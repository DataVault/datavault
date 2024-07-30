package org.datavaultplatform.webapp.selenium;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnabledIfEnvironmentVariables(value = {
        @EnabledIfEnvironmentVariable(named = BaseSeleniumTest.ENV_DV_USERNAME, matches = ".*"),
        @EnabledIfEnvironmentVariable(named = BaseSeleniumTest.ENV_DV_PASSWORD, matches = ".*")
})
public @interface RequiresUsernamePasswordEnvVariables {
}
