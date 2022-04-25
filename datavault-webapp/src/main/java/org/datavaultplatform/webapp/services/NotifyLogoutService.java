package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.request.CreateClientEvent;

/**
 * @see ForceLogoutService
 */
public interface NotifyLogoutService {
  String notifyLogout(CreateClientEvent clientEvent);
}
