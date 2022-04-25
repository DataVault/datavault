package org.datavaultplatform.webapp.config.standalone;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Profile("standalone")
@ComponentScan({"org.datavaultplatform.webapp.controllers.standalone"})
@Import(StandaloneWebSecurityConfig.class)
public class StandaloneProfileConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }
}
