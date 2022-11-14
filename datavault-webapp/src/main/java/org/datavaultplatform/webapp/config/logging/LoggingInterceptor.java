package org.datavaultplatform.webapp.config.logging;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

  public static Logger LOGGER = log;

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {

    if (log.isDebugEnabled()) {
      logRequest(request, body);
    }

    ClientHttpResponse response = execution.execute(request, body);

    if (log.isDebugEnabled()) {
      logResponse(response);
    }

    return response;
  }

  private void logRequest(HttpRequest req, byte[] body) {
    log.debug("REQ:START");
    log.debug("REQ:uri     [{}]", req.getURI());
    log.debug("REQ:method  [{}]", req.getMethod());
    log.debug("REQ:headers {}", req.getHeaders());
    log.debug("REQ:body    [{}]", new String(body, UTF_8));
    log.debug("REQ:END");
  }

  private void logResponse(ClientHttpResponse res) throws IOException {
    log.debug("RES:START");
    log.debug("RES:statusCode [{}]", res.getStatusCode());
    log.debug("RES:statusText [{}]", res.getStatusText());
    log.debug("RES:headers    {}", res.getHeaders());
    log.debug("RES:body       [{}]", StreamUtils.copyToString(res.getBody(), UTF_8));
    log.debug("RES:END");
  }
}
