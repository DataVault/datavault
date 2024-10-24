package org.datavaultplatform.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetrievedChunksTest {

    @Nested
    class ValidationTests {

        final RetrievedChunks retrievedChunks = new RetrievedChunks();

        @Nested
        class AddStoredChunkTests {
            @ParameterizedTest
            @ValueSource(ints = {0, -1})
            void testInvalidChunkNumber(int chunkNumber) {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> retrievedChunks.addRetrievedChunk(chunkNumber));
                assertThat(ex).hasMessage("The chunkNumber must be greater than 0");
            }
        }

        @Nested
        class IsChunkRetrievedTests {
            @ParameterizedTest
            @ValueSource(ints = {0, -1})
            void testInvalidChunkNumber(int chunkNumber) {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> retrievedChunks.isRetrieved(chunkNumber));
                assertThat(ex).hasMessage("The chunkNumber must be greater than 0");
            }
        }
    }

    @Test
    void testArchiveStore() {
        RetrievedChunks retrievedChunks = new RetrievedChunks();
        retrievedChunks.addRetrievedChunk(1);
        assertThat(retrievedChunks.isRetrieved(1)).isTrue();
        assertThat(retrievedChunks.getRetrievedChunks()).isEqualTo(List.of(1));

        assertThat(retrievedChunks.isRetrieved(2)).isFalse();
        assertThat(retrievedChunks.getRetrievedChunks()).isEqualTo(List.of(1));

        retrievedChunks.addRetrievedChunk(1);
        assertThat(retrievedChunks.isRetrieved(1)).isTrue();
        assertThat(retrievedChunks.getRetrievedChunks()).isEqualTo(List.of(1));

        retrievedChunks.addRetrievedChunk(2);
        assertThat(retrievedChunks.isRetrieved(1)).isTrue();
        assertThat(retrievedChunks.isRetrieved(2)).isTrue();
        assertThat(retrievedChunks.getRetrievedChunks()).isEqualTo(List.of(1, 2));

        retrievedChunks.addRetrievedChunk(1);
        retrievedChunks.addRetrievedChunk(2);
        retrievedChunks.addRetrievedChunk(3);
        assertThat(retrievedChunks.isRetrieved(1)).isTrue();
        assertThat(retrievedChunks.isRetrieved(2)).isTrue();
        assertThat(retrievedChunks.isRetrieved(3)).isTrue();
        assertThat(retrievedChunks.getRetrievedChunks()).isEqualTo(List.of(1, 2, 3));
    }

    @Nested
    class JsonTests {

        ObjectMapper mapper;

        @BeforeEach
        void setup() {
            mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        @SneakyThrows
        RetrievedChunks fromJson(String json) {
            if (StringUtils.isBlank(json)) {
                return new RetrievedChunks();
            } else {
                return mapper.readValue(json, RetrievedChunks.class);
            }
        }

        @SneakyThrows
        String toJson(RetrievedChunks retrieved) {
            if (retrieved == null) {
                return null;
            } else {
                return mapper.writeValueAsString(retrieved);
            }
        }

        static final String EMPTY_JSON_1 = """
                {
                    "retrievedChunks" : []
                }
                """;
        static final String EMPTY_JSON_2 = """
                {
                    "retrievedChunks" : null
                }
                """;
        static final String EMPTY_JSON_3 = """
                {
                }
                """;

        static final String NON_EMPTY_JSON = """
                {
                        "retrievedChunks" : [1,2,3]
                }
                """;

        static final String NON_EMPTY_WRONG_ORDER_JSON = """
                {
                        "retrievedChunks" : [3,2,1]
                        }
                }
                """;
        static final String NON_STANDARD_JSON = """
                {
                        "retrievedChunks" : [null,3,null,2,null,1,null],
                        "extra" : "x-value"
                }
                """;

        @Test
        @SneakyThrows
        void testSerializeEmpty() {
            RetrievedChunks empty = new RetrievedChunks();
            JSONAssert.assertEquals(EMPTY_JSON_1,
                    toJson(empty), true);
        }

        @ParameterizedTest
        @ValueSource(strings = {EMPTY_JSON_1, EMPTY_JSON_2, EMPTY_JSON_3})
        @SneakyThrows
        void testDeSerializeEmpty(String emptyJson) {
            assertThat(fromJson(emptyJson)).isEqualTo(new RetrievedChunks());
        }

        @Test
        @SneakyThrows
        void testDeSerializeNull() {
            assertThat(fromJson(null)).isEqualTo(new RetrievedChunks());
        }

        @Test
        @SneakyThrows
        void testSerializeNonEmpty() {
            JSONAssert.assertEquals(NON_EMPTY_JSON,
                    toJson(getNonEmpty()), true);
        }

        @Test
        @SneakyThrows
        void testDeSerializeNonEmpty1() {
            assertThat(fromJson(NON_EMPTY_JSON)).isEqualTo(getNonEmpty());
        }

        @Test
        @SneakyThrows
        void testDeSerializeNonEmpty2() {
            RetrievedChunks retrieved = fromJson(NON_EMPTY_WRONG_ORDER_JSON);
            assertThat(retrieved).isEqualTo(getNonEmpty());
        }

        @Test
        @SneakyThrows
        void testDeSerializeNonEmpty3() {
            RetrievedChunks retrieved = fromJson(NON_STANDARD_JSON);
            assertThat(retrieved).isEqualTo(getNonEmpty());
        }

        RetrievedChunks getNonEmpty() {
            RetrievedChunks retrieved = new RetrievedChunks();
            retrieved.addRetrievedChunk(1);
            retrieved.addRetrievedChunk(2);
            retrieved.addRetrievedChunk(3);
            return retrieved;
        }
    }


}