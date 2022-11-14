package org.datavaultplatform.broker.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;

public class JacksonConfig {

  @Bean
  Hibernate5Module hibernate5Module() {
    return new Hibernate5Module();
  }

}
