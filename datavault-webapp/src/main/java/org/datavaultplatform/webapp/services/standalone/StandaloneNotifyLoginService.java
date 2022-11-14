package org.datavaultplatform.webapp.services.standalone;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.webapp.services.NotifyLoginService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("standalone")
@Service
@Slf4j
public class StandaloneNotifyLoginService implements NotifyLoginService {

  @Override
  public String notifyLogin(CreateClientEvent clientEvent) {
      log.info("notifyLogin {}", clientEvent);
      return "NOTIFIED";
  }

  @Override
  public Group[] getGroups() {
    log.info("getGroups()");
    return new Group[0];
  }
}
