package org.datavaultplatform.common.util;

import ch.qos.logback.classic.Level;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@ExtendWith({TempFileCleanerExtensionTest.MyLogLevelExtension.class, TempFileCleanerExtension.class})
class TempFileCleanerExtensionTest {

    // Define the logger levels you want to change for this test class
    static class MyLogLevelExtension extends LogLevelExtension {
        MyLogLevelExtension() {
            super(Map.of(
                "org.datavaultplatform.common.util", Level.DEBUG
            ));
        }
    }
    
    @Test
    @SneakyThrows
    void testJavaTmpDirDelete() {
        log.debug("testJavaTmpDirDelete");
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Files.writeString(tempDir.resolve("testNonSlash102.txt"), "Hello world!\n");

        Path path2 = tempDir.resolve("deleteNonSlash/testNonSlash103.txt");
        Files.createDirectories(path2.getParent());
        Files.writeString(path2, "Hello world!\n");
    }

    @Test
    @SneakyThrows
    void testSlashTmpDirDelete() {
        log.debug("testSlashTmpDirDelete");
        Path path = Paths.get("/tmp/deleteSlash/test100.txt");

        // Create parent directories if they don't exist
        Files.createDirectories(path.getParent());

        // Write text to the file
        Files.writeString(path, "Hello world!\n");

        Files.writeString(Paths.get("/tmp/testSlash101.txt"), "Hello world!\n");
    }
    
    
}