package org.datavaultplatform.webapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Configuration
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().replaceWithClass(ModelAndView.class, String.class);
        SpringDocUtils.getConfig().replaceWithClass(RedirectView.class, String.class);
    }
    
    @Bean
    public OpenAPI openAPI(Clock clock, Optional<GitProperties> optGitProperties) {

        final String securitySchemeName = "SSOHeaderAuth";
        final var securityScheme = new SecurityScheme().name("uid").type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).description("SSO Auth: Please enter your User ID (e.g. jdoe123)");

        String version = String.format("%s (%s)",
                optGitProperties.map(gitProperties -> gitProperties.get("build.properties")).orElse("0.0.0"),
                optGitProperties.map(GitProperties::getShortCommitId).orElse("0.0.0"));

        String date = ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        Info info = new Info()
                .title("DataVault WebApp API")
                .version(version)
                .description("API documentation for the DataVault WebApp module.");
        info.addExtension("x-generated-at", date);

        return new OpenAPI()
                .info(info)
                // 2. Global Security Requirement
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 3. Components (Security Schemes)
                .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
    }
}
    