package org.datavaultplatform.broker.config;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.context.annotation.Bean;

public class JacksonConfig {

  @Bean
  Hibernate5JakartaModule hibernate5JakartaModule() {
    return new Hibernate5JakartaModule();
  }

}
