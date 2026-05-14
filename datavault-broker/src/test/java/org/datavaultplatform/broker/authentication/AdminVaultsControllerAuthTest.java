package org.datavaultplatform.broker.authentication;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Import(AdminVaultsControllerAuthTest.TestConfig.class)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
class AdminVaultsControllerAuthTest extends BaseControllerAuthTest {

    @Autowired
    TestConfig.AdminVaultsTestController1 controller;


    void checkEndpointAccessFail(HttpStatus expectedStatus, String url, Object... args) {
        assertThat(controller).isNotNull();
        MockHttpServletRequestBuilder builder = setupAuthentication(get(url, args));
        super.checkFailureWhenNotAuthorized(builder, expectedStatus, true);
    }

    void checkEndpointAccessOkay(String url, Object expected, Object... args) {
        assertThat(controller).isNotNull();

        // This works because in the database, 'IS Admin' will be associated with every Permission via the database - it's not 100% clear from the java code that it is/should be.
        // the 'checkSuccessWhenAuthenticated' will give all permissions to an 'IS Admin' user to simulate the database.
        // and some Permisssions map to extra ADMIN_XXX spring security roles like ROLE_ADMIN_VAULTS which is used to secure /admin/vaults/**
        MockHttpServletRequestBuilder builder = setupAuthentication(get(url, args));
        checkSuccessWhenAuthenticated(builder, expected, HttpStatus.OK, true);

        // Use the getActualRoles() method provided by BaseControllerAuthTest!
        Set<String> actualGrantedAuthorityNames = getActualRoles();

        Set<String> expectedGrantedAuthorityNames = new HashSet<>();
        expectedGrantedAuthorityNames.add(RoleName.ROLE_ADMIN);
        expectedGrantedAuthorityNames.add(RoleName.ROLE_CLIENT_USER);

        // but although the 'IS Admin' is associate with every Permission - the broker only considers a sub-set of these
        Arrays.stream(Permission.values())
                .filter(RestAuthenticationProvider.ADMIN_API_PERMISSIONS::contains)
                .map(Permission::getRoleName)
                .filter(Objects::nonNull)
                .forEach(expectedGrantedAuthorityNames::add);

        assertThat(actualGrantedAuthorityNames).isEqualTo(expectedGrantedAuthorityNames);
    }

    @Test
    void testIsAdminCanAccessAdminsArchiveStoresEndpoints1() {
        checkEndpointAccessOkay("/admin/archivestores/test/{vaultId}", Map.of("vaultId1", "vault-id-123"), "vault-id-123");
    }

    @Test
    void testIsAdminCanAccessAdminsDepositsEndpoints2() {
        checkEndpointAccessOkay("/admin/deposits/test/{vaultId}", Map.of("vaultId2", "vault-id-123"), "vault-id-123");
    }

    @Test
    void testIsAdminCanAccessAdminsRetrievesEndpoints3() {
        checkEndpointAccessOkay("/admin/retrieves/test/{vaultId}", Map.of("vaultId3", "vault-id-123"), "vault-id-123");
    }

    @Test
    void testIsAdminCanAccessAdminsVaultsEndpoints4() {
        checkEndpointAccessOkay("/admin/vaults/test/{vaultId}", Map.of("vaultId4", "vault-id-123"), "vault-id-123");
    }

    @Test
    void testIsAdminCanAccessAdminsPendingVaultsEndpoints5() {
        checkEndpointAccessOkay("/admin/pendingVaults/test/{vaultId}", Map.of("vaultId5", "vault-id-123"), "vault-id-123");
    }

    @Test
    void testIsAdminCanAccessAdminsBillingEndpoints5() {
        checkEndpointAccessOkay("/admin/billing/test/{vaultId}", Map.of("vaultId6", "vault-id-123"), "vault-id-123");
    }

    @Test
    void testIsAdminCannotAccessAdminsReviewsEndpoints6() {
        // SpringConfig sets up SecurityRule for /admin/reviews/** but RestAuthenticationProvider does NOT add Permission for Permission.CAN_NANAGE_REVIEWS
        // so the 'IS Admin' user will not have ROLE_CAN_MANAGE_REVIEWS which is needed for /admin/reviews/**
        checkEndpointAccessFail(HttpStatus.FORBIDDEN, "/admin/reviews/test/{vaultId}", "vault-id-123");
    }

    @Configuration
    static class TestConfig {


        @RestController
        public static class AdminVaultsTestController1 {

            @GetMapping("/admin/archivestores/test/{vaultId}")
            public Map<String, String> adminArchiveStoresTestEndpoint(@PathVariable String vaultId) {
                return Map.of("vaultId1", vaultId);
            }

            @GetMapping("/admin/deposits/test/{vaultId}")
            public Map<String, String> adminDepositsTestEndpoint(@PathVariable String vaultId) {
                return Map.of("vaultId2", vaultId);
            }

            @GetMapping("/admin/retrieves/test/{vaultId}")
            public Map<String, String> adminRetrievesTestEndpoint(@PathVariable String vaultId) {
                return Map.of("vaultId3", vaultId);
            }

            @GetMapping("/admin/vaults/test/{vaultId}")
            public Map<String, String> adminVaultsTestEndpoint(@PathVariable String vaultId) {
                return Map.of("vaultId4", vaultId);
            }

            @GetMapping("/admin/pendingVaults/test/{vaultId}")
            public Map<String, String> adminPendingVaultsTestEndpoint(@PathVariable String vaultId) {
                return Map.of("vaultId5", vaultId);
            }

            @GetMapping("/admin/billing/test/{vaultId}")
            public Map<String, String> adminBillingTestEndpoint(@PathVariable String vaultId) {
                return Map.of("vaultId6", vaultId);
            }

            @GetMapping("/admin/reviews/test/{vaultId}")
            public Map<String, String> adminReviewsTestEndpoint(@PathVariable String vaultId) {
                return Map.of("vaultId7", vaultId);
            }
        }
    }
}
