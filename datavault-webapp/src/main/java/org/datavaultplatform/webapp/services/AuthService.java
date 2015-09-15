package org.datavaultplatform.webapp.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthService implements UserDetailsService {

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        throw new UsernameNotFoundException("Error in retrieving user");

    }

    public Collection<GrantedAuthority> getAuthorities(Integer access) {
        
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
        
        authList.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        return authList;
    }
}
