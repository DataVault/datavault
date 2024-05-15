package org.datavaultplatform.webapp.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

public abstract class AuthenticationUtils {
    
    public static void validateGrantedAuthorities(Collection<? extends GrantedAuthority> grantedAuthorities) {
        Assert.notNull(grantedAuthorities, "The collection of GrantedAuthorities cannot be null");
        for (var gq : grantedAuthorities) {
            Assert.notNull(gq, "Cannot have a null GrantedAuthority");
            Assert.notNull(gq.getAuthority(), "Cannot have a GrantedAuthority [%s] with a null Authority".formatted(gq));
        }
    }
}
