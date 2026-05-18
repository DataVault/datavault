package org.datavaultplatform.webapp.controllers.trace.mvc;

import io.micrometer.tracing.Tracer;
import org.datavaultplatform.webapp.controllers.SimpleRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.io.File;

@Profile("trace")
@TestConfiguration
public class TraceIdDemoControllerConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return factory -> factory.addContextCustomizers(context -> {
            File testWebapp = new File("src/test/webapp");
            context.setDocBase(testWebapp.getAbsolutePath());
        });
    }

    @Bean
    public SpringResourceTemplateResolver testTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("file:src/test/webapp/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setOrder(0); // highest priority
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @Autowired
    Tracer tracer;

    @Bean
    SimpleRestService simpleRestService(){
        return new SimpleRestService(restTemplateBuilder.build());
    }

    @Bean
    public TraceIdDemoController traceIdDemoController(){
        return new TraceIdDemoController(tracer, simpleRestService());
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/**"); // apply to all requests
        return http.build();
    }
}
