package org.datavaultplatform.common.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.*;

@Slf4j
class TempFileCleanerExtensionTest {
    
    @Test
    @SneakyThrows
    void testJavaTmpDirDelete() {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Files.writeString(tempDir.resolve("testNonSlash102.txt"), "Hello world!\n");

        Path path2 = tempDir.resolve("deleteNonSlash/testNonSlash103.txt");
        Files.createDirectories(path2.getParent());
        Files.writeString(path2, "Hello world!\n");
    }

    @Test
    @SneakyThrows
    void testSlashTmpDirDelete() {
        Path path = Paths.get("/tmp/deleteSlash/test100.txt");

        // Create parent directories if they don't exist
        Files.createDirectories(path.getParent());

        // Write text to the file
        Files.writeString(path, "Hello world!\n");

        Files.writeString(Paths.get("/tmp/testSlash101.txt"), "Hello world!\n");
    }
    
    
}