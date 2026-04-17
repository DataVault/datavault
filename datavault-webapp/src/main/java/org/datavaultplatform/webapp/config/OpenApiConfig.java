package org.datavaultplatform.webapp.config;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.util.AntPathMatcher; // Added import

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.datavaultplatform.webapp.config.HttpSecurityUtils.SECURITY_PATH_MAP;

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

    @Bean
    @Order(1)
    public GlobalOpenApiCustomizer duplicateOperationIdFixer() {
        return openApi -> {
            Map<String, Integer> idCounts = new HashMap<>();
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        String id = operation.getOperationId();
                        if (id != null) {
                            int count = idCounts.getOrDefault(id, 0);
                            if (count > 0) {
                                operation.setOperationId(id + "_" + count);
                            }
                            idCounts.put(id, count + 1);
                        }
                    })
            );
        };
    }

    @Bean
    @Order(2)
    public GlobalOpenApiCustomizer sortSchemaPropertiesCustomizer() {
        return openApi -> {

            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                openApi.getComponents().getSchemas().values().forEach(schema -> {
                    if (schema.getProperties() != null) {
                        // Replace the properties map with a sorted TreeMap
                        Map<String, Schema> sortedProperties = new TreeMap<>(schema.getProperties());
                        schema.setProperties(sortedProperties);
                    }
                });
            }
        };
    }

    @Order(3)
    @Bean
    public OperationCustomizer customizeSecurityDescriptions() {
        return (operation, handlerMethod) -> {
            PreAuthorize preAuth = handlerMethod.getMethodAnnotation(PreAuthorize.class);
            if (preAuth != null) {
                String currentDesc = operation.getDescription() != null ? operation.getDescription() : "";
                // Append the SpEL expression to the description
                operation.setDescription(currentDesc + "\n\n**Security Expression (PreAuthorize):** `" + preAuth.value() + "`");
            }
            return operation;
        };
    }

    @Bean
    @Order(4) // Ensure this runs after other customizers
    public GlobalOpenApiCustomizer httpSecurityRulesCustomizer() {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) -> {
                String matchedSecurityExpression = null;
                // Iterate through the map to find the most specific match
                for (Map.Entry<String, String> entry : SECURITY_PATH_MAP.entrySet()) {
                    String securityPattern = entry.getKey();
                    String expression = entry.getValue();

                    if (pathMatcher.match(securityPattern, path)) {
                        matchedSecurityExpression = expression;
                        break; // Found the most specific match due to LinkedHashMap order
                    }
                }

                if (matchedSecurityExpression != null && !matchedSecurityExpression.equals("permitAll")) {
                    final String finalExpression = matchedSecurityExpression; // For lambda capture
                    pathItem.readOperations().forEach(operation -> {
                        String currentDesc = operation.getDescription() != null ? operation.getDescription() : "";
                        // Append the HttpSecurity rule to the description
                        operation.setDescription(currentDesc + "\n\n**HttpSecurity Rule:** `" + finalExpression + "`");
                    });
                }
            });
        };
    }
}
    