package org.datavaultplatform.webapp.services;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import java.util.Set;
import java.util.stream.Collectors;

public class ForceLogoutService {

    private static final Logger logger = LoggerFactory.getLogger(ForceLogoutService.class);

    private SessionRegistry sessionRegistry;

    private RestService restService;

    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void logoutUser(String userId) {
        logger.warn("Forcing a logout for user {}", userId);
        logoutUsers(Sets.newHashSet(userId));
    }

    public void logoutRole(long roleId) {
        logger.warn("Forcing a logout for all users assigned to role {}", roleId);
        Set<String> userIds = restService.getRoleAssignmentsForRole(roleId).stream()
                .map(roleAssignment -> roleAssignment.getUserId())
                .collect(Collectors.toSet());
        logoutUsers(userIds);
    }

    private void logoutUsers(Set<String> userIds) {
        sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof String && userIds.contains(principal))
                .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
                .forEach(SessionInformation::expireNow);
    }
}
