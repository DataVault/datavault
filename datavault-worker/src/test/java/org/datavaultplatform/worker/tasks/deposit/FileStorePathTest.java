package org.datavaultplatform.worker.tasks.deposit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FileStorePathTest {
    
    @ParameterizedTest
    @CsvSource(textBlock = """
            'abc/def',   'abc', 'def'
            ' abc/def ', 'abc', 'def'
            ' abc/    ', 'abc', ''
            '  /def   ', '',    'def'
            """)
    void testWithSlash(String combined, String before, String after) {
        FileStorePath path = new FileStorePath(combined);
        assertThat(path.storageID()).isEqualTo(before);
        assertThat(path.storagePath()).isEqualTo(after);
    }
    
    @Test
    void testWithNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new FileStorePath(null));
        assertThat(ex).hasMessage("The fileStorePath cannot be null");
    }

}