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

            if (includeStandaloneOnly) {
                requests.antMatchers("/test/**", "/index").permitAll();
            }

            requests.antMatchers("/resources/**").permitAll(); //OKAY
            requests.antMatchers("/actuator/info").permitAll();  //OKAY
            requests.antMatchers("/actuator/health").permitAll();  //OKAY
            requests.antMatchers("/actuator/customtime").permitAll();  //OKAY
            requests.antMatchers("/actuator","/actuator/").permitAll(); //OKAY
            requests.antMatchers("/actuator/**").hasRole("ADMIN"); //TODO - check this
            requests.antMatchers("/error").permitAll();      //OKAY
            requests.antMatchers("/auth/**").permitAll();      //OKAY

            requests.antMatchers("/admin").hasRole("ADMIN");
            requests.antMatchers("/admin/").access("hasRole('ROLE_ADMIN')");
            requests.antMatchers("/admin/archivestores/**").access("hasRole('ROLE_ADMIN_ARCHIVESTORES')");
            requests.antMatchers("/admin/billing/**").access("hasRole('ROLE_ADMIN_BILLING')");
            requests.antMatchers("/admin/deposits/**").access("hasRole('ROLE_ADMIN_DEPOSITS')");
            requests.antMatchers("/admin/events/**").access("hasRole('ROLE_ADMIN_EVENTS')");
            requests.antMatchers("/admin/retentionpolicies/**").access("hasRole('ROLE_ADMIN_RETENTIONPOLICIES')");
            requests.antMatchers("/admin/retrieves/**").access("hasRole('ROLE_ADMIN_RETRIEVES')");
            requests.antMatchers("/admin/roles/**").access("hasRole('ROLE_ADMIN_ROLES')");
            requests.antMatchers("/admin/schools/**").access("hasRole('ROLE_ADMIN_SCHOOLS')");
            requests.antMatchers("/admin/vaults/**").access("hasRole('ROLE_ADMIN_VAULTS')");
            requests.antMatchers("/admin/reviews/**").access("hasRole('ROLE_ADMIN_REVIEWS')");

            // most general matcher - has to go last
            requests.antMatchers("/**").access("hasRole('ROLE_USER')"); //OKAY
        });
    }


    public static void sessionManagement(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http.sessionManagement()
                .maximumSessions(1)
                .expiredUrl("/auth/login?security")
                .sessionRegistry(sessionRegistry);
    }

    public static void formLogin(HttpSecurity http, AuthenticationSuccess authenticationSuccess) throws Exception {
        http.formLogin()
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/security_check")
                .failureUrl("/auth/login?error=true")
                .defaultSuccessUrl("/")
                .successHandler(authenticationSuccess)
                .and()

            .logout()
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .and()

            .exceptionHandling()
                    .accessDeniedPage("/auth/denied");
    }
}
