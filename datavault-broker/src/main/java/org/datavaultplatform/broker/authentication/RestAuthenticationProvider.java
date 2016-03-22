package org.datavaultplatform.broker.authentication;

import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.common.model.Client;
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

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        List<GrantedAuthority> grantedAuths = new ArrayList<>();

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
        logger.debug("preAuthenticatedPrincipal = " + name + ", trying to authenticate");

        User user = usersService.getUser(name);
        if (user == null) {
            logger.debug("No matching User record found for Id " + name);
            throw new RestAuthenticationException("Invalid user Id : " + name);
        }

        // If we got here then we found a matching User record.
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));


        RestWebAuthenticationDetails rwad = (RestWebAuthenticationDetails) authentication.getDetails();
        logger.debug("Client Key is " + rwad.getClientKeyRequestHeader());
        logger.debug("Client IP address is " + rwad.getRemoteAddress());

        if (validateClient) {
            String clientApiKey = rwad.getClientKeyRequestHeader();
            String ipAddress = rwad.getRemoteAddress();

            Client client = clientsService.getClientByApiKey(clientApiKey);
            if (client == null) {
                logger.debug("No matching Client record found for API key " + clientApiKey);
                throw new RestAuthenticationException("Invalid client credentials");
            }

            // If we got here then we found a matching Client record, so check the IP addresses.
            if (!ipAddress.equals(client.getIpAddress())) {
                logger.debug("Invalid IP address on incoming request " + ipAddress);
                throw new RestAuthenticationException("Invalid client credentials");
            }

            grantedAuths.add(new SimpleGrantedAuthority("ROLE_CLIENT_USER"));
        }

        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
