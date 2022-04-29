package org.datavaultplatform.broker.config;


import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityConfig {

  //TODO - not finished!
  @Bean
  VelocityEngine velocityEngine(){
    return new VelocityEngine();
  }

}
