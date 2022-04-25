package org.datavaultplatform.webapp.authentication.shib;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

@Slf4j
public class ShibAuthenticationListener implements ApplicationListener<AuthenticationSuccessEvent> {

  @Override
  public void onApplicationEvent(AuthenticationSuccessEvent event) {
    log.info("auth event -  [{}]",event);
  }

}
