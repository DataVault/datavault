package org.datavaultplatform.broker.authentication;

import org.datavaultplatform.broker.services.AdminService;
import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

/**
 * In truth this class does not do any true authentication as it is assumed the user was authenticated by the client
 * application, it just has a look at the incoming user and client details and grants roles accordingly.
 *
 * User: Robin Taylor
 * Date: 13/11/2015
 * Time: 15:10
 */

public class RestAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationProvider.class);

    private UsersService usersService;
    private ClientsService clientsService;
    private RolesAndPermissionsService rolesAndPermissionsService;
    private AdminService adminService;

    // This is a crappy means of allowing someone to turn off the client validation. Must be a better way of doing this.
    private boolean validateClient;

    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }

    public void setClientsService(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

    public void setValidateClient(boolean validateClient) {
        this.validateClient = validateClient;
    }

    public void setRolesAndPermissionsService(RolesAndPermissionsService rolesAndPermissionsService) {
        this.rolesAndPermissionsService = rolesAndPermissionsService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        List<GrantedAuthority> grantedAuths = new ArrayList<>();

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        logger.debug("preAuthenticatedPrincipal = " + name + ", trying to authenticate");

        // Note: name may be empty in some circumstances eg. a request to add a new user for a first time Shibboleth authenticated user.
        if ((name != null) && (!name.equals(""))) {
            User user = usersService.getUser(name);
            if (user == null) {
                logger.debug("No matching User record found for Id " + name);
                throw new RestAuthenticationException("Invalid user Id : " + name);
            }

            // If we got here then we found a matching User record.
            if (adminService.isAdminUser(user)) {
                logger.debug(user.getID() + " is an admin user");
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                logger.debug(user.getID() + " is an ordinary user");
                grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            checkPermissions(name, Permission.CAN_MANAGE_ARCHIVE_STORES, grantedAuths);
            checkPermissions(name, Permission.CAN_MANAGE_DEPOSITS, grantedAuths);
            checkPermissions(name, Permission.CAN_VIEW_RETRIEVES, grantedAuths);
            checkPermissions(name, Permission.CAN_MANAGE_VAULTS, grantedAuths);
            checkPermissions(name, Permission.CAN_VIEW_EVENTS, grantedAuths);
            checkPermissions(name, Permission.CAN_MANAGE_BILLING_DETAILS, grantedAuths);
        }

        RestWebAuthenticationDetails rwad = (RestWebAuthenticationDetails) authentication.getDetails();
        logger.debug("Client Key is " + rwad.getClientKeyRequestHeader());
        logger.debug("Client IP address is " + rwad.getRemoteAddress());

        if (validateClient) {
            String clientApiKey = rwad.getClientKeyRequestHeader();
            String ipAddress = rwad.getRemoteAddress();

            Client client = clientsService.getClientByApiKey(clientApiKey);
            if (client == null) {
                logger.info("No matching Client record found for API key " + clientApiKey);
                throw new RestAuthenticationException("Invalid client credentials");
            }

            // If we got here then we found a matching Client record, so check the IP addresses.
            if (!ipAddress.equals(client.getIpAddress())) {
                logger.info("Invalid IP address on incoming request " + ipAddress);
                throw new RestAuthenticationException("Invalid client credentials");
            }

            grantedAuths.add(new SimpleGrantedAuthority("ROLE_CLIENT_USER"));
        }

        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }

    private void checkPermissions(String userId, Permission permission, List<GrantedAuthority> grantedAuths) {
        if (rolesAndPermissionsService.hasPermission(userId, permission)) {
            grantedAuths.add(new SimpleGrantedAuthority(permission.getRoleName()));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
