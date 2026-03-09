package org.datavaultplatform.broker.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

@Configuration
@SecurityScheme(
    name = "X-UserID-Header",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = HEADER_USER_ID,
    description = "User ID header, corresponds to `org.datavaultplatform.common.util.Constants.HEADER_USER_ID`."
)
@SecurityScheme(
    name = "X-Client-Key-Header",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = HEADER_CLIENT_KEY,
    description = "Client Key header, corresponds to `org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY`."
)
public class OpenApiConfig {

    private List<SecurityRequirement> security;

    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation ->
                        operation.getResponses().values().forEach(apiResponse -> {
                            Content content = apiResponse.getContent();
                            // If content is null or contains the wildcard */*
                            if (content != null && content.containsKey("*/*")) {
                                var mediaType = content.get("*/*");
                                content.remove("*/*");
                                content.addMediaType("application/json", mediaType);
                            }
                        })
                )
        );
    }

    @Bean
    public OpenAPI openAPI(Clock clock, Optional<GitProperties> optGitProperties) {

        String version = String.format("%s (%s)",
                optGitProperties.map(gitProperties -> gitProperties.get("build.properties")).orElse("0.0.0"),
                optGitProperties.map(GitProperties::getShortCommitId).orElse("0.0.0"));

        SecurityRequirement req1 = new SecurityRequirement().addList("X-UserID-Header");
        SecurityRequirement req2 = new SecurityRequirement().addList("X-Client-Key-Header");

        String date = ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Info info = new Info()
                .title("DataVault Broker API")
                .version(version).description("""
                                API documentation for the DataVault Broker module.
                                All endpoints require authentication via the X-UserID and X-Client-Key headers.""");
        info.addExtension("x-generated-at", date);
        return new OpenAPI()
                .info(info)
                .security(List.of(req1, req2));
    }
}
