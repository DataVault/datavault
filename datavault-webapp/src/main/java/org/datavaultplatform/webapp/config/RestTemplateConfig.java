package org.datavaultplatform.webapp.config;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.config.logging.LoggingInterceptor;
import org.datavaultplatform.webapp.services.ApiErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("!standalone")
@Slf4j
public class RestTemplateConfig {

  /*
   * If we don't preconfigure the timeouts on restTemplate it can retry connections for up to 5 minutes!
   */
  @Bean
  RestTemplate restTemplate(@Value("${broker.timeout.ms:1000}") int brokerTimeoutMs) {
    log.info("broker.timeout.ms [{}]", brokerTimeoutMs);
    RestTemplate result = new RestTemplate(getRequestFactory(brokerTimeoutMs));
    result.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
    result.setErrorHandler(new ApiErrorHandler());
    return  result;
  }

  private ClientHttpRequestFactory getRequestFactory(int brokerTimeoutMs) {
    HttpComponentsClientHttpRequestFactory inner = new HttpComponentsClientHttpRequestFactory();
    inner.setConnectTimeout(brokerTimeoutMs);
    inner.setReadTimeout(brokerTimeoutMs);
    inner.setConnectionRequestTimeout(brokerTimeoutMs);
    return new BufferingClientHttpRequestFactory(inner);
  }

}
