package org.datavaultplatform.broker.authentication;

import org.datavaultplatform.broker.services.ClientsService;
import org.datavaultplatform.common.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

/**
 * In truth this class does not do any true authentication as it is assumed the user was authenticated by the client
 * application, it just checks to see that the combination of user and client application is authorised to do what
 * they want to do.
 *
 * User: Robin Taylor
 * Date: 13/11/2015
 * Time: 15:10
 */

public class RestAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationProvider.class);

    private ClientsService clientsService;

    private boolean validateClient;

    public void setClientsService(ClientsService clientsService) {
        this.clientsService = clientsService;
    }

    public void setValidateClient(boolean validateClient) {
        this.validateClient = validateClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        RestWebAuthenticationDetails rwad = (RestWebAuthenticationDetails) authentication.getDetails();

        logger.debug("Client Key is " + rwad.getClientKeyRequestHeader());
        logger.debug("Client IP address is " + rwad.getRemoteAddress());

        if (validateClient) {
            String clientApiKey = rwad.getClientKeyRequestHeader();
            String ipAddress = rwad.getRemoteAddress();

            // todo : If Hibernate doesn't find a match it throws a null pointer exception, should I catch it and do
            // something else like throw a better exception and return a 403?
            Client client = clientsService.getClientByApiKey(clientApiKey);

            // If we got here then we found a matching Client record, so check the IP addresses.
            if (!ipAddress.equals(client.getIpAddress())) {
                // todo : throw a better exception and return a 403?
                throw new RuntimeException("Invalid IP address on incoming request " + ipAddress);
            }
        }

        // todo : still to decide on what roles we should actually use
        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new PreAuthenticatedAuthenticationToken(name, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
