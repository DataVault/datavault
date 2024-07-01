package org.datavaultplatform.webapp.config;

import java.time.Duration;
import java.util.List;
import javax.net.ssl.SSLContext;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.SocketConfig.Builder;
import org.apache.hc.core5.util.Timeout;
import org.apache.http.ssl.TrustStrategy;
import org.datavaultplatform.webapp.config.logging.LoggingInterceptor;
import org.datavaultplatform.webapp.services.ApiErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("!standalone")
@Slf4j
public class RestTemplateConfig {

  @Value("${broker.using.selfsignedcert:false}")
  private boolean brokerUsingSelfSignedCert;

  /*
   * If we don't preconfigure the timeouts on restTemplate it can retry connections for up to 5 minutes!
   */
  @Bean
  @SneakyThrows
  RestTemplate restTemplate(@Value("${broker.timeout.ms:1000}") int brokerTimeoutMs) {
    log.info("broker.timeout.ms [{}]", brokerTimeoutMs);

    Builder builder = SocketConfig.custom();
    if (brokerTimeoutMs > 0) {
      builder = builder.setSoTimeout(Timeout.ofMilliseconds(brokerTimeoutMs));
    }
    SocketConfig config = builder.build();

    PoolingHttpClientConnectionManagerBuilder builder1 = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultSocketConfig(config);

    final PoolingHttpClientConnectionManager cm;
    if (brokerUsingSelfSignedCert) {
      TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
      SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
              .loadTrustMaterial(null, acceptingTrustStrategy).build();
      SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
              new NoopHostnameVerifier());
      cm = builder1.setSSLSocketFactory(sslSocketFactory).build();
    } else {
      cm = builder1.build();
    }
    CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();
    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
            httpclient);

    RestTemplateBuilder tBuilder = new RestTemplateBuilder();
    if (brokerTimeoutMs > 0) {
      tBuilder = tBuilder.setConnectTimeout(Duration.ofMillis(brokerTimeoutMs));
    }
    RestTemplate restTemplate = tBuilder
            .requestFactory(() -> factory)
            //.setBufferRequestBody(true)
            .interceptors(List.of(new LoggingInterceptor()))
            .errorHandler(new ApiErrorHandler())
            .build();

    return restTemplate;
  }

  @PostConstruct
  void init() {
    log.info("Broker Using Self-Signed Cert?[{}]", brokerUsingSelfSignedCert);
  }

}
