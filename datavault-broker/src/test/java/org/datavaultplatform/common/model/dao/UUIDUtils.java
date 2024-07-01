package org.datavaultplatform.common.model.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Used to check that generated database ids are generated as UUIDs.
 * Used to test all is well when changing @Id generation annotations
 */
public class UUIDUtils {
    public static final String PATTERN = "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";

    private static final String EXAMPLE_UUID = "f77b2f94-1426-4cfb-8601-256c962cf0b1";

    private static final String NIL_UUID  = "00000000-0000-0000-0000-000000000000";
    private static final String MAX_UUID  = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF";

    private static final Predicate<String> UUID_TEST = Pattern.compile(PATTERN).asMatchPredicate();

    public static boolean isUUID(String value) {
        return usingUUID(value) && usingRegex(value);
    }

    private static boolean usingUUID(String value){
        if(value == null){
            return false;
        }
        try {
            //this accepts UUID strings which are not 36 long
            UUID uuid = UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex){
            return false;
        }
    }
    private static boolean usingRegex(String value){
        if(value == null){
            return false;
        }
        return UUID_TEST.test(value);
    }

    public static void assertIsUUID(String id) {
        assertThat(isUUID(id)).isTrue();
    }


    @Test
    void simpleTest() {
        assertTrue(EXAMPLE_UUID.matches(PATTERN));
        assertTrue(isUUID(EXAMPLE_UUID));
    }

    @Test
    void testSpecialUUIDs() {
        assertTrue(isUUID(NIL_UUID));
        assertTrue(isUUID(MAX_UUID));
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            null , false
            ''   , false
            ' '  , false
            'f77b2f94-1426-4cfb-8601-256c962cf0b1 ', false
            ' f77b2f94-1426-4cfb-8601-256c962cf0b1', false
            f77b2f94-1426-4cfb-8601-256c962cf0b12, false
            f77b2f94-1426-4cfb-8601-256c962cf0b, false
            f77b2f94-1426-4cfb-8601-256c962cf0b1, true
            F77B2f94-1426-4cfb-8601-256c962cf0b1, true
            """)
    void testUUID(String id, boolean expected) {
        assertThat(isUUID(id)).isEqualTo(expected);
    }
}


