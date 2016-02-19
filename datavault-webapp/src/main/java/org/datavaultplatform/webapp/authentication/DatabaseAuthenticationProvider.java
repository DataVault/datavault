package org.datavaultplatform.webapp.authentication;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Robin Taylor
 * Date: 06/11/2015
 * Time: 12:03
 */

@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Note: - name here is equivalent to our User.id
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        // Fetch the existing user
        User user;
        try {
            user = restService.getUser(name);
        } catch (Exception e) {
            // Broker auth failure
            // TODO: log the exception/auth error?
            user = null;
        }

        if (user == null) {
            throw new UsernameNotFoundException("Invalid user id/name");
        }

        if (!password.equals(user.getPassword())) {
            throw new BadCredentialsException("Inavalid password");
        }

        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}