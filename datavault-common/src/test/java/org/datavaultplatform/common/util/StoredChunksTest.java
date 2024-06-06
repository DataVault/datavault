package org.datavaultplatform.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.StringUtils;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoredChunksTest {

    @Nested
    class ValidationTests {

        final StoredChunks storedChunks = new StoredChunks();

        @Nested
        class AddStoredChunkTests {
            @ParameterizedTest
            @ValueSource(strings = "  ")
            @NullSource
            void testInvalidArchiveId(String archiveStoreId) {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storedChunks.addStoredChunk(archiveStoreId, 1));
                assertThat(ex).hasMessage("The archiveStoreId cannot be blank");
            }

            @ParameterizedTest
            @ValueSource(ints = {0, -1})
            void testInvalidChunkNumber(int chunkNumber) {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storedChunks.addStoredChunk("test-archive-id", chunkNumber));
                assertThat(ex).hasMessage("The chunkNumber must be greater than 0");
            }
        }

        @Nested
        class IsChunkStoredTests {
            @ParameterizedTest
            @ValueSource(strings = "  ")
            @NullSource
            void testInvalidArchiveId(String archiveStoreId) {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storedChunks.isStored(archiveStoreId, 1));
                assertThat(ex).hasMessage("The archiveStoreId cannot be blank");
            }

            @ParameterizedTest
            @ValueSource(ints = {0, -1})
            void testInvalidChunkNumber(int chunkNumber) {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storedChunks.isStored("test-archive-id", chunkNumber));
                assertThat(ex).hasMessage("The chunkNumber must be greater than 0");
            }
        }

        @Nested
        class GetStoredChunksTest {
            @ParameterizedTest
            @ValueSource(strings = "  ")
            @NullSource
            void testStoredChunksTest(String archiveStoreId) {
                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storedChunks.getStoredChunksForArchiveStoreId(archiveStoreId));
                assertThat(ex).hasMessage("The archiveStoreId cannot be blank");
            }
        }
    }

    @Test
    void testArchiveStore() {
        StoredChunks storedChunks = new StoredChunks();
        storedChunks.addStoredChunk("test-archive-id", 1);
        assertThat(storedChunks.isStored("test-archive-id", 1)).isTrue();
        assertThat(storedChunks.getStoredChunksForArchiveStoreId("test-archive-id")).isEqualTo(List.of(1));

        assertThat(storedChunks.isStored("missing", 1)).isFalse();
        assertThat(storedChunks.getStoredChunksForArchiveStoreId("missing")).isEmpty();

        storedChunks.addStoredChunk("test-archive-id", 1);
        assertThat(storedChunks.isStored("test-archive-id", 1)).isTrue();
        assertThat(storedChunks.getStoredChunksForArchiveStoreId("test-archive-id")).isEqualTo(List.of(1));

        storedChunks.addStoredChunk("test-archive-id", 2);
        assertThat(storedChunks.isStored("test-archive-id", 1)).isTrue();
        assertThat(storedChunks.isStored("test-archive-id", 2)).isTrue();
        assertThat(storedChunks.getStoredChunksForArchiveStoreId("test-archive-id")).isEqualTo(List.of(1, 2));

        storedChunks.addStoredChunk("id-2", 1);
        storedChunks.addStoredChunk("id-2", 2);
        storedChunks.addStoredChunk("id-2", 3);
        assertThat(storedChunks.isStored("id-2", 1)).isTrue();
        assertThat(storedChunks.isStored("id-2", 2)).isTrue();
        assertThat(storedChunks.isStored("id-2", 3)).isTrue();
        assertThat(storedChunks.getStoredChunksForArchiveStoreId("id-2")).isEqualTo(List.of(1, 2, 3));
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
        StoredChunks fromJson(String json) {
            if (StringUtils.isBlank(json)) {
                return new StoredChunks();
            } else {
                return mapper.readValue(json, StoredChunks.class);
            }
        }

        @SneakyThrows
        String toJson(StoredChunks stored) {
            if (stored == null) {
                return null;
            } else {
                return mapper.writeValueAsString(stored);
            }
        }

        static final String EMPTY_JSON = """
                {
                    "storedChunks" : { }
                }
                """;

        static final String NON_EMPTY_JSON = """
                {
                        "storedChunks" : {
                          "asid-1":  [1,2,3],
                          "asid-2":  [4,5,6],
                          "asid-3":  [7,8,9]
                        }
                }
                """;

        static final String NON_EMPTY_WRONG_ORDER_JSON = """
                {
                        "storedChunks" : {
                          "asid-2":  [6,5,4],
                          "asid-1":  [1,3,2],
                          "asid-3":  [9,8,7]
                        }
                }
                """;
        static final String NON_STANDARD_JSON = """
                {
                        "storedChunks" : {
                          " " : [1,2,3],
                          "asid-2":  [6,5,4,null,-1,0],
                          "asid-1":  [1,3,2],
                          "asid-3":  [9,8,7],
                          "asid-4":  null,
                          "asid-5" :  [null, -1, 0]
                        }
                }
                """;

        @Test
        @SneakyThrows
        void testSerializeEmpty() {
            StoredChunks empty = new StoredChunks();
            JSONAssert.assertEquals(EMPTY_JSON,
                    toJson(empty), true);
        }

        @Test
        @SneakyThrows
        void testDeSerializeEmpty() {
            assertThat(fromJson(EMPTY_JSON)).isEqualTo(new StoredChunks());
        }

        @Test
        @SneakyThrows
        void testDeSerializeNull() {
            assertThat(fromJson(null)).isEqualTo(new StoredChunks());
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
            StoredChunks stored = fromJson(NON_EMPTY_WRONG_ORDER_JSON);
            assertThat(stored).isEqualTo(getNonEmpty());
        }

        @Test
        @SneakyThrows
        void testDeSerializeNonEmpty3() {
            StoredChunks stored = fromJson(NON_STANDARD_JSON);
            assertThat(stored).isEqualTo(getNonEmpty());
        }

        StoredChunks getNonEmpty() {
            StoredChunks stored = new StoredChunks();
            stored.addStoredChunk("asid-1", 3);
            stored.addStoredChunk("asid-1", 2);
            stored.addStoredChunk("asid-1", 1);
            stored.addStoredChunk("asid-3", 7);
            stored.addStoredChunk("asid-3", 8);
            stored.addStoredChunk("asid-3", 9);
            stored.addStoredChunk("asid-2", 6);
            stored.addStoredChunk("asid-2", 5);
            stored.addStoredChunk("asid-2", 4);
            return stored;
        }
    }


}