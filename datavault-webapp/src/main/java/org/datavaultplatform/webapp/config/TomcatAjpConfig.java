package org.datavaultplatform.webapp.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TomcatAjpConfig {

  private static final String PROTOCOL = "AJP/1.3";

  @Value("${tomcat.ajp.enabled:false}")
  private boolean ajpEnabled;

  @Value("${tomcat.ajp.port:8009}")
  private int ajpPort;

  @Value("${tomcat.ajp.scheme:http}")
  private String ajpScheme;

  @Value("${tomcat.ajp.redirect.port:8443}")
  private int ajpRedirectPort;

  @Value("${tomcat.ajp.connector.secure:false}")
  private boolean ajpConnectorSecure;

  @Value("${tomcat.ajp.protocol.secret.required:false}")
  private boolean ajpProtocolSecretRequired;

  @Value("${tomcat.ajp.protocol.tomcat.authentication:false}")
  private boolean ajpProtocolTomcatAuthentication;

  @Bean
  public TomcatServletWebServerFactory servletContainer() {
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

    log.info("tomcat.ajp.enabled [{}]", ajpEnabled);
    log.info("tomcat.ajp.port [{}]", ajpPort);
    log.info("tomcat.ajp.scheme [{}]", ajpScheme);
    log.info("tomcat.ajp.redirect.port [{}]", ajpRedirectPort);
    log.info("tomcat.ajp.connector.secure  [{}]", ajpConnectorSecure);
    log.info("tomcat.ajp.protocol.secret.required  [{}]", ajpProtocolSecretRequired);
    log.info("tomcat.ajp.protocol.tomcat.authentication  [{}]", ajpProtocolTomcatAuthentication);

    if (ajpEnabled) {
      Connector ajpConnector = new Connector(PROTOCOL);
      ajpConnector.setPort(ajpPort);
      ajpConnector.setSecure(ajpConnectorSecure);
      ajpConnector.setScheme(ajpScheme);
      ajpConnector.setRedirectPort(ajpRedirectPort);

      AjpNioProtocol ajpProtocol = (AjpNioProtocol) ajpConnector.getProtocolHandler();

      ajpProtocol.setSecretRequired(ajpProtocolSecretRequired);
      ajpProtocol.setTomcatAuthentication(false);

      tomcat.addAdditionalTomcatConnectors(ajpConnector);
    }
    return tomcat;
  }
}
