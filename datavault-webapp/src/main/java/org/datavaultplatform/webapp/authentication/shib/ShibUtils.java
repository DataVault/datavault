package org.datavaultplatform.webapp.authentication.shib;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public abstract class ShibUtils {
    public static final GrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");
    public static final GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    public static final GrantedAuthority ROLE_IS_ADMIN = new SimpleGrantedAuthority("ROLE_IS_ADMIN");
    public static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
}
