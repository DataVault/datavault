package org.datavaultplatform.webapp.authentication.shib;

import org.datavaultplatform.common.model.RoleName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public abstract class ShibUtils {
    public static final GrantedAuthority ROLE_USER = new SimpleGrantedAuthority(RoleName.ROLE_USER);
    public static final GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority(RoleName.ROLE_ADMIN);
    public static final GrantedAuthority ROLE_IS_ADMIN = new SimpleGrantedAuthority(RoleName.ROLE_IS_ADMIN);
    public static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
}
