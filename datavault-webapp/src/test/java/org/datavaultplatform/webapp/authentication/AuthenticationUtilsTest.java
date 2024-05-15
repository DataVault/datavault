package org.datavaultplatform.webapp.authentication;

import org.datavaultplatform.webapp.controllers.standalone.api.SimulateErrorController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationUtilsTest {

    GrantedAuthority gaWithNullAuthority;
    
    @BeforeEach
    void setup() {
        gaWithNullAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return null;
            }

            @Override
            public String toString() {
                return "test-ga";
            }
        };
    }

    @Test
    void testValidateGrantedAuthoritiesWithNullCollection() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthenticationUtils.validateGrantedAuthorities(null));
        assertThat(ex).hasMessage("The collection of GrantedAuthorities cannot be null");
    }

    @Test
    void testValidateGrantedAuthoritiesWithNullGrantedAuthority() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("one"));
        grantedAuthorities.add(null);
        grantedAuthorities.add(new SimpleGrantedAuthority("two"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthenticationUtils.validateGrantedAuthorities(grantedAuthorities));
        assertThat(ex).hasMessage("Cannot have a null GrantedAuthority");
    }

    @Test
    void testValidateGrantedAuthoritiesWithGrantedAuthorityWithNullAuthority() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        
        grantedAuthorities.add(new SimpleGrantedAuthority("one"));
        grantedAuthorities.add(gaWithNullAuthority);
        grantedAuthorities.add(new SimpleGrantedAuthority("two"));
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthenticationUtils.validateGrantedAuthorities(grantedAuthorities));
        assertThat(ex).hasMessage("Cannot have a GrantedAuthority [test-ga] with a null Authority");
    }
}