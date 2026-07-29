package org.datavaultplatform.webapp.config.ratelimited;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "ratelimited.filter")
public record FilterProperties(List<String> pathPatterns) {

    private static final Pattern VALID_PATH_PATTERN = Pattern.compile(
            "^\\/((" +
                    "[a-zA-Z0-9._~-]+" +      // literal
                    "|\\*{1,2}" +             // * or **
                    "|\\{[a-zA-Z0-9_]+\\}" +  // {var}
                    ")(\\/|$))+$"
    );

    public FilterProperties {
        // Provide a default empty list if nothing is configured
        if (pathPatterns == null) {
            pathPatterns = List.of();
        }

        for (String pathPattern : pathPatterns) {
            validatePathPattern(pathPattern);
        }
    }

    public static void validatePathPattern(String pathPattern) {
        if (!VALID_PATH_PATTERN.matcher(pathPattern).matches()) {
            throw new IllegalArgumentException("Invalid path or pattern: " + pathPattern);
        }

        // Optional: forbid mixing Ant + spring path variables
        boolean hasAnt = pathPattern.contains("*");
        boolean hasVars = pathPattern.contains("{");
        if (hasAnt && hasVars) {
            throw new IllegalArgumentException("Cannot mix Ant patterns and path variables: " + pathPattern);
        }
    }
}