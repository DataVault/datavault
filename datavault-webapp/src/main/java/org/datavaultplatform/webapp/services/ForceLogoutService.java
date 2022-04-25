package org.datavaultplatform.webapp.services;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@ConditionalOnBean(RestService.class)
public class ForceLogoutService {

    private static final Logger logger = LoggerFactory.getLogger(ForceLogoutService.class);

    private final SessionRegistry sessionRegistry;

    private final RestService supportService;

    @Autowired
    public ForceLogoutService(
        SessionRegistry sessionRegistry,
        RestService supportService) {
        this.sessionRegistry = sessionRegistry;
        this.supportService = supportService;
    }

    public void logoutUser(String userId) {
        logger.warn("Forcing a logout for user {}", userId);
        logoutUsers(Sets.newHashSet(userId));
    }

    public void logoutRole(long roleId) {
        logger.warn("Forcing a logout for all users assigned to role {}", roleId);
        Set<String> userIds = supportService.getRoleAssignmentsForRole(roleId).stream()
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
