package org.datavaultplatform.common.actuator;

import org.springframework.security.test.context.support.WithMockUser;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(username = "actuator-user", roles = "ACTUATOR")
public @interface WithMockActuatorUser {
}