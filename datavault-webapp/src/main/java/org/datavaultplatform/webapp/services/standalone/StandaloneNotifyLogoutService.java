package org.datavaultplatform.webapp.services.standalone;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.webapp.services.NotifyLogoutService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("standalone")
@Service
@Slf4j
public class StandaloneNotifyLogoutService implements NotifyLogoutService {

  @Override
  public String notifyLogout(CreateClientEvent clientEvent) {
    log.info("Standalone Notify Logout {}", clientEvent);
    return "NOTIFIED";
  }

}
