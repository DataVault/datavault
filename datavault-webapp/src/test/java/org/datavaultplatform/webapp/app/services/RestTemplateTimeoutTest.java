package org.datavaultplatform.webapp.app.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.AbstractClientHttpRequestFactoryWrapper;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
@ProfileDatabase
@TestPropertySource(properties = "broker.timeout.ms=2000")
public class RestTemplateTimeoutTest {

  @LocalServerPort
  int serverPort;

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
      String url = String.format("http://localhost:%d/auth/info/hello", serverPort);
      long start = System.currentTimeMillis();
      ResourceAccessException ex = assertThrows(ResourceAccessException.class,() ->restTemplate.getForEntity(url, String.class));
      long diff = System.currentTimeMillis() - start;
      log.info("diff is {}",diff);
      assertTrue(diff > 2_000);
      assertTrue(diff < 2_600);
      String expectedErrorPrefix = String.format("I/O error on GET request for \"http://localhost:%d/auth/info/hello\": Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out",serverPort);
      assertThat(ex.getMessage()).startsWith(expectedErrorPrefix);
  }

  @TestConfiguration
  @RestController
  static class TestController {

    @GetMapping("/auth/info/hello")
    @SneakyThrows
    String sayHello() {
      TimeUnit.SECONDS.sleep(10);
      return "hello";
    }
  }

}
