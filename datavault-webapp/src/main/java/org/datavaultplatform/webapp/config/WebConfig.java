package org.datavaultplatform.webapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.datavaultplatform.webapp.config.trace.TracingConfig;

@Configuration
@Slf4j
@Import(TracingConfig.class)
public class WebConfig implements WebMvcConfigurer {

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  @Order(1)
  public CommonsRequestLoggingFilter requestLoggingFilter() {
    log.info("creating logging filter");
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setIncludeHeaders(true);
    loggingFilter.setMaxPayloadLength(100_000);
    return loggingFilter;
  }

}
