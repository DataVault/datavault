package org.datavaultplatform.webapp.config;

import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;

public class HttpSecurityUtils {

    public static void authorizeRequests(
            HttpSecurity http) throws Exception {
        authorizeRequests(http, false);
    }

    public static void authorizeRequests(
            HttpSecurity http, boolean includeStandaloneOnly) throws Exception {
        http.authorizeRequests(requests -> {

            requests.requestMatchers("/favicon.ico").permitAll(); //OKAY

            if (includeStandaloneOnly) {
                requests.requestMatchers("/test/**", "/index").permitAll();
            }

            requests.requestMatchers("/resources/**").permitAll(); //OKAY
            requests.requestMatchers("/error").permitAll();      //OKAY
            requests.requestMatchers("/auth/**").permitAll();      //OKAY

            requests.requestMatchers("/admin").hasRole("ADMIN");
            requests.requestMatchers("/admin/").access("hasRole('ROLE_ADMIN')");
            requests.requestMatchers("/admin/archivestores/**").access("hasRole('ROLE_ADMIN_ARCHIVESTORES')");
            requests.requestMatchers("/admin/billing/**").access("hasRole('ROLE_ADMIN_BILLING')");
            requests.requestMatchers("/admin/deposits/**").access("hasRole('ROLE_ADMIN_DEPOSITS')");
            requests.requestMatchers("/admin/events/**").access("hasRole('ROLE_ADMIN_EVENTS')");
            requests.requestMatchers("/admin/retentionpolicies/**").access("hasRole('ROLE_ADMIN_RETENTIONPOLICIES')");
            requests.requestMatchers("/admin/retrieves/**").access("hasRole('ROLE_ADMIN_RETRIEVES')");
            requests.requestMatchers("/admin/roles/**").access("hasRole('ROLE_ADMIN_ROLES')");
            requests.requestMatchers("/admin/schools/**").access("hasRole('ROLE_ADMIN_SCHOOLS')");
            requests.requestMatchers("/admin/vaults/**").access("hasRole('ROLE_ADMIN_VAULTS')");
            requests.requestMatchers("/admin/reviews/**").access("hasRole('ROLE_ADMIN_REVIEWS')");

            // most general matcher - has to go last
            requests.requestMatchers("/**").access("hasRole('ROLE_USER')"); //OKAY
        });
    }


    public static void sessionManagement(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http.sessionManagement(sm -> {
                sm.maximumSessions(1)
                    .expiredUrl("/auth/login?security")
                    .sessionRegistry(sessionRegistry);
        });
    }

    public static void formLogin(HttpSecurity http, AuthenticationSuccess authenticationSuccess) throws Exception {
        http.formLogin(fmLogin -> {
            fmLogin.loginPage("/auth/login")
                    .loginProcessingUrl("/auth/security_check")
                    .failureUrl("/auth/login?error=true")
                    .defaultSuccessUrl("/")
                    .successHandler(authenticationSuccess);
        });

        http.logout(logout -> {
            logout.logoutUrl("/auth/logout")
                    .logoutSuccessUrl("/auth/login?logout");
        });

        http.exceptionHandling(exh -> exh.accessDeniedPage("/auth/denied"));
    }
}
