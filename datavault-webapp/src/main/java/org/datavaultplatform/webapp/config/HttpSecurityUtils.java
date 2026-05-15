package org.datavaultplatform.webapp.config;

import org.datavaultplatform.common.config.SecurityMethod;
import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.datavaultplatform.webapp.config.trace.TraceLoggingFilter;
import org.datavaultplatform.webapp.config.trace.MdcRequestFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpSecurityUtils {

    public static void authorizeRequests(
            HttpSecurity http,
            TraceLoggingFilter traceLoggingFilter,
            MdcRequestFilter userMdcFilter) throws Exception {
        authorizeRequests(http, false, traceLoggingFilter, userMdcFilter);
    }

    // Map to store URL patterns and their corresponding HttpSecurity rules
    // Order matters: more specific paths should come before more general paths.
    public static final Map<String, String> SECURITY_PATH_MAP = new LinkedHashMap<>();

    static {
        // Populate this map based on HttpSecurityUtils.authorizeRequests
        // Example entries based on your HttpSecurityUtils.java:
        SECURITY_PATH_MAP.put("/favicon.ico", "permitAll()");
        SECURITY_PATH_MAP.put("/resources/**", "permitAll()");
        SECURITY_PATH_MAP.put("/error", "permitAll()");
        SECURITY_PATH_MAP.put("/auth/**", "permitAll()");

        // Specific admin paths
        SECURITY_PATH_MAP.put("/admin/paused/deposit/toggle", "hasRole('IS_ADMIN')");
        SECURITY_PATH_MAP.put("/admin/paused/retrieve/toggle", "hasRole('IS_ADMIN')");

        SECURITY_PATH_MAP.put("/admin/paused/deposit/history", "hasRole('USER')");
        SECURITY_PATH_MAP.put("/admin/paused/retrieve/history", "hasRole('USER')");

        SECURITY_PATH_MAP.put("/admin/archivestores/**", "hasAuthority('ROLE_ADMIN_ARCHIVESTORES')");
        SECURITY_PATH_MAP.put("/admin/billing/**", "hasAuthority('ROLE_ADMIN_BILLING')");

        SECURITY_PATH_MAP.put("/admin/deposits/**", "hasAuthority('ROLE_ADMIN_DEPOSITS')");
        SECURITY_PATH_MAP.put("/admin/events/**", "hasAuthority('ROLE_ADMIN_EVENTS')");
        SECURITY_PATH_MAP.put("/admin/retentionpolicies/**", "hasAuthority('ROLE_ADMIN_RETENTIONPOLICIES')");
        SECURITY_PATH_MAP.put("/admin/retrieves/**", "hasAuthority('ROLE_ADMIN_RETRIEVES')");
        SECURITY_PATH_MAP.put("/admin/roles/**", "hasAuthority('ROLE_ADMIN_ROLES')");

        SECURITY_PATH_MAP.put("/admin/schools/**", "hasAuthority('ROLE_ADMIN_SCHOOLS')");
        SECURITY_PATH_MAP.put("/admin/vaults/**", "hasAuthority('ROLE_ADMIN_VAULTS')");
        SECURITY_PATH_MAP.put("/admin/reviews/**", "hasAuthority('ROLE_ADMIN_REVIEWS')");
        SECURITY_PATH_MAP.put("/admin/pendingVaults/**", "hasRole('IS_ADMIN')");

        // General admin paths (more specific than /**)
        SECURITY_PATH_MAP.put("/admin/", "hasAuthority('ROLE_ADMIN')");
        SECURITY_PATH_MAP.put("/admin", "hasRole('ADMIN')");

        // Most general matcher - must be last
        SECURITY_PATH_MAP.put("/**", "hasAuthority('ROLE_USER')");
    }

    public static void authorizeRequests(
            HttpSecurity http,
            boolean includeStandaloneOnly,
            TraceLoggingFilter tracingFilter,
            MdcRequestFilter userMdcFilter) throws Exception {
        
        http.addFilterBefore(tracingFilter, SecurityContextPersistenceFilter.class);
        http.addFilterAfter(userMdcFilter, SecurityContextHolderFilter.class);
        
        http.authorizeHttpRequests(authz -> {

            if (includeStandaloneOnly) {
                authz.requestMatchers("/test/**", "/index").permitAll();
            }

            for(Map.Entry<String, String> entry : SECURITY_PATH_MAP.entrySet()) {
                var matchers = authz.requestMatchers(entry.getKey());
                SecurityMethod sm = SecurityMethod.from(entry.getValue());
                if (sm.isPermitAll()) {
                    matchers.permitAll();

                } else if (sm.isHasRole()) {
                    matchers.hasRole(sm.arg());

                } else if (sm.isHasAuthority()) {
                    matchers.hasAuthority(sm.arg());

                } else {
                    throw new RuntimeException("Unknown security method: " + sm.method());
                }
            }

            /*

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
             */
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
