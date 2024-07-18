package org.datavaultplatform.broker.controllers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StorageIdAndRetrievePathTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
                FILE-STORE-SRC-ID/src-path-1,       FILE-STORE-SRC-ID,  src-path-1
                FILE-STORE-SRC-ID/src-path-1/a/b/c, FILE-STORE-SRC-ID,  src-path-1/a/b/c
                FILE-STORE-SRC-ID/,                 FILE-STORE-SRC-ID, /
                /,                                  '',                 /
                '',                                 '',                 /
            """)
    void testSlashOnly(String full, String expectedStorageId, String expectedRetrievePath) {
        StorageIdAndRetrievePath result = StorageIdAndRetrievePath.fromFullPath(full);
        assertThat(result.storageID()).isEqualTo(expectedStorageId);
        assertThat(result.retrievePath()).isEqualTo(expectedRetrievePath);
    }
}