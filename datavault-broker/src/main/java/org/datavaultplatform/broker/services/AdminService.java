package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {
    private static final Logger Log = LoggerFactory.getLogger(AdminService.class);

    private final UsersService usersService;
    private final RolesAndPermissionsService rolesAndPermissionsService;

    public AdminService(UsersService usersService,
        RolesAndPermissionsService rolesAndPermissionsService) {
        this.usersService = usersService;
        this.rolesAndPermissionsService = rolesAndPermissionsService;
    }

    public User ensureAdminUser(String userID) throws Exception {
        User user = usersService.getUser(userID);
        if (user == null) {
            Log.warn(String.format("Tried to find user with id='%s' but got null", userID));
            throw new Exception("User '" + userID + "' does not exist");
        }
        if (!isAdminUser(user)) {
            Log.warn(String.format("User with id='%s' is not an admin", userID));
            throw new Exception("Access denied");
        }

        return user;
    }

    public boolean isAdminUser(User user) {
        return isAdminUser(user.getID());
    }

    public boolean isAdminUser(String userID) {
        return rolesAndPermissionsService.isAdminUser(userID);
    }

}
