package org.datavaultplatform.webapp.config;

import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.datavaultplatform.webapp.config.trace.TraceLoggingFilter;
import org.datavaultplatform.webapp.config.trace.MdcRequestFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

public class HttpSecurityUtils {

    public static void authorizeRequests(
            HttpSecurity http,
            TraceLoggingFilter traceLoggingFilter,
            MdcRequestFilter userMdcFilter) throws Exception {
        authorizeRequests(http, false, traceLoggingFilter, userMdcFilter);
    }

    public static void authorizeRequests(
            HttpSecurity http,
            boolean includeStandaloneOnly,
            TraceLoggingFilter tracingFilter,
            MdcRequestFilter userMdcFilter) throws Exception {
        
        http.addFilterBefore(tracingFilter, SecurityContextPersistenceFilter.class);
        http.addFilterAfter(userMdcFilter, SecurityContextHolderFilter.class);
        
        http.authorizeHttpRequests(authz -> {

            authz.requestMatchers("/favicon.ico").permitAll(); //OKAY

            if (includeStandaloneOnly) {
                authz.requestMatchers("/test/**", "/index").permitAll();
            }

            authz.requestMatchers("/resources/**").permitAll(); //OKAY
            authz.requestMatchers("/error").permitAll();      //OKAY
            authz.requestMatchers("/auth/**").permitAll();      //OKAY

            authz.requestMatchers("/admin").hasRole("ADMIN");
            authz.requestMatchers("/admin/").hasAuthority("ROLE_ADMIN");
            
            authz.requestMatchers("/admin/paused/deposit/history").hasRole("USER");
            authz.requestMatchers("/admin/paused/deposit/toggle").hasRole("IS_ADMIN");

            authz.requestMatchers("/admin/paused/retrieve/history").hasRole("USER");
            authz.requestMatchers("/admin/paused/retrieve/toggle").hasRole("IS_ADMIN");
            authz.requestMatchers("/admin/pendingVaults/**").hasRole("IS_ADMIN");

            authz.requestMatchers("/admin/archivestores/**").hasAuthority("ROLE_ADMIN_ARCHIVESTORES");
            authz.requestMatchers("/admin/billing/**").hasAuthority("ROLE_ADMIN_BILLING");
            authz.requestMatchers("/admin/deposits/**").hasAuthority("ROLE_ADMIN_DEPOSITS");
            authz.requestMatchers("/admin/events/**").hasAuthority("ROLE_ADMIN_EVENTS");
            authz.requestMatchers("/admin/retentionpolicies/**").hasAuthority("ROLE_ADMIN_RETENTIONPOLICIES");
            authz.requestMatchers("/admin/retrieves/**").hasAuthority("ROLE_ADMIN_RETRIEVES");
            authz.requestMatchers("/admin/roles/**").hasAuthority("ROLE_ADMIN_ROLES");
            authz.requestMatchers("/admin/schools/**").hasAuthority("ROLE_ADMIN_SCHOOLS");
            authz.requestMatchers("/admin/vaults/**").hasAuthority("ROLE_ADMIN_VAULTS");
            authz.requestMatchers("/admin/reviews/**").hasAuthority("ROLE_ADMIN_REVIEWS");

            // most general matcher - has to go last
            authz.requestMatchers("/**").hasAuthority("ROLE_USER"); //OKAY
        });
    }


    public static void sessionManagement(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http.sessionManagement(sm -> {
                sm.maximumSessions(1)
                    .expiredUrl("/auth/login?security")
                    .sessionRegistry(sessionRegistry);
        });
    }

    public static void formLogin(HttpSecurity http, AuthenticationSuccess authenticationSuccess,
                                 AccessDeniedHandler accessDeniedHandler ) throws Exception {
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

        if (accessDeniedHandler != null) {
            http.exceptionHandling(exh -> exh.accessDeniedHandler(accessDeniedHandler));
        } else {
            http.exceptionHandling(exh -> exh.accessDeniedPage("/auth/denied"));
        }
    }
}
