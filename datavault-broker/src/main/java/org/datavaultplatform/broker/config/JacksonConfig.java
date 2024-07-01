package org.datavaultplatform.broker.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;

public class JacksonConfig {

  @Bean
  Hibernate6Module hibernate6JakartaModule() {
    return new Hibernate6Module();
  }

}
