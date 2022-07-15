package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.request.CreateClientEvent;
import org.springframework.security.core.Authentication;

/**
 * @see ForceLogoutService
 */
public interface NotifyLogoutService {
  String notifyLogout(CreateClientEvent clientEvent);
  String notifyLogout(CreateClientEvent clientEvent, Authentication auth);
}
