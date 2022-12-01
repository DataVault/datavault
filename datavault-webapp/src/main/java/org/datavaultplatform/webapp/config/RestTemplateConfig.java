package org.datavaultplatform.webapp.config;

import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
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

  @Value("${broker.using.selfsignedcert:}")
  private boolean brokerUsingSelfSignedCert;

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
    if (brokerUsingSelfSignedCert) {
      inner.setHttpClient(getClientForSelfSignedCert());
    }
    // by using -1, we can remove the timeout - helps debug the broker!
    if(brokerTimeoutMs > 0) {
      inner.setConnectTimeout(brokerTimeoutMs);
      inner.setReadTimeout(brokerTimeoutMs);
      inner.setConnectionRequestTimeout(brokerTimeoutMs);
    }
    log.warn("broker.timeout.ms [{}]", brokerTimeoutMs);
    return new BufferingClientHttpRequestFactory(inner);
  }

  /**
   * When using SelfSignedCerts for testing SSL locally, we have to disable the SSL cert verification.
   */
  @SneakyThrows
  protected HttpClient getClientForSelfSignedCert() {
    TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
    SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
    return httpClient;
  }

  @PostConstruct
  void init() {
    log.info("Broker Using Self-Signed Cert?[{}]", brokerUsingSelfSignedCert);
  }

}
