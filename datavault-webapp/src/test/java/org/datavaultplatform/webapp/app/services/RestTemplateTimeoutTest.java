package org.datavaultplatform.webapp.app.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.AbstractClientHttpRequestFactoryWrapper;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@Slf4j
@ProfileDatabase
@TestPropertySource(properties = "broker.timeout.ms=2000")
public class RestTemplateTimeoutTest {

  @Autowired
  RestTemplate restTemplate;

  @Value("${broker.timeout.ms}")
  private int timeoutMs;


  /*
    Really dig into restTemplate and check timeouts
   */
  @Test
  @SneakyThrows
  void testTimeoutConfig() {

    Field fRequestFactory = AbstractClientHttpRequestFactoryWrapper.class.getDeclaredField("requestFactory");
    fRequestFactory.setAccessible(true);

    Field fRequestConfig = HttpComponentsClientHttpRequestFactory.class.getDeclaredField("requestConfig");
    fRequestConfig.setAccessible(true);

    assertEquals(2_000, timeoutMs);

    ClientHttpRequestFactory iFactory = restTemplate.getRequestFactory();

    BufferingClientHttpRequestFactory bFactory = (BufferingClientHttpRequestFactory)fRequestFactory.get(iFactory);

    HttpComponentsClientHttpRequestFactory factory = (HttpComponentsClientHttpRequestFactory)fRequestFactory.get(bFactory);
    RequestConfig requestConfig = (RequestConfig)fRequestConfig.get(factory);
    assertEquals(2_000, requestConfig.getSocketTimeout());
    assertEquals(2_000, requestConfig.getConnectionRequestTimeout());
    assertEquals(2_000, requestConfig.getConnectTimeout());
  }

  @Test
  void testTimeout() {
      long start = System.currentTimeMillis();
      ResourceAccessException ex = assertThrows(ResourceAccessException.class,() -> restTemplate.getForEntity("http://example.com:9999/resource",String.class));
      long diff = System.currentTimeMillis() - start;
      log.info("diff is {}",diff);
      assertTrue(diff > 2_000);
      assertTrue(diff < 2_500);
      assertThat(ex.getMessage()).startsWith("I/O error on GET request for \"http://example.com:9999/resource\": Connect to example.com:9999 ");
  }

}
