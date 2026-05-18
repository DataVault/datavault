package org.datavaultplatform.common.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityMethodTest {

    @Test
    void testHasPermission() {  
        var hasAuthorityResult = SecurityMethod.from("hasAuthority('ROLE_ADMIN_ARCHIVESTORES')");
        assertThat(hasAuthorityResult).isEqualTo(new SecurityMethod("hasAuthority", "ROLE_ADMIN_ARCHIVESTORES"));
    }
    
    @Test
    void testHasRole() {
        var hasRoleResult = SecurityMethod.from("hasRole('IS_ADMIN')");
        assertThat(hasRoleResult).isEqualTo(new SecurityMethod("hasRole", "IS_ADMIN"));
    }

    @Test
    void testPermitAll() {
        var permitAllResult = SecurityMethod.from("permitAll()");
        assertThat(permitAllResult).isEqualTo(new SecurityMethod("permitAll",""));
    }
}