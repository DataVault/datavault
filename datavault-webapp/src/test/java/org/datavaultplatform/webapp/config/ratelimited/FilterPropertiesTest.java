package org.datavaultplatform.webapp.config.ratelimited;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FilterPropertiesTest {

    @ParameterizedTest
    @ValueSource(strings = {
            // without trailing slashes
            "/vaults/autocompleteuun/{term}",
            "/vaults/isuun/{uun}",
            "/api/limited/test",
            "/api/**",
            "/api/*/bob",
            "/api",

            // with trailing slashes
            "/vaults/autocompleteuun/{term}/",
            "/vaults/isuun/{uun}/",
            "/api/limited/test/",
            "/api/**/",
            "/api/*/bob/",
            "/api/"

    })
    void testValidPathPatterns(String validPathPattern) {
        assertDoesNotThrow(() -> FilterProperties.validatePathPattern(validPathPattern));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/users/[0-9]+", //regex
            "bob", //not a pattern
            "", //empty string
            " ", //space
            "\t", //tab,
            "/api/{id}/*", //mixing variables and ant
    })
    void testInvalidPathPatterns(String invalidPathPattern) {
        assertThrows(IllegalArgumentException.class, () -> FilterProperties.validatePathPattern(invalidPathPattern));
    }
    
    
    @Test
    void testNullPathPatterns() {
        FilterProperties config = new FilterProperties(null);
        assertTrue(config.pathPatterns().isEmpty());
    }

    @Test
    void testPathPatterns() {
        FilterProperties config = new FilterProperties(List.of("/a/b/{c}","/d/e/f", "/g/h/**"));
        assertThat(config.pathPatterns()).containsExactly("/a/b/{c}","/d/e/f", "/g/h/**");  
    }
}