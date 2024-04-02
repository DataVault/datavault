package org.datavaultplatform.common.io;


import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileUtilsTest {

    @Test
    public void testParseFormattedSizeToBytesWithNumber() {
        long expectedValue = 123456789L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("123456789");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithKiloByteString() {
        long expectedValue = 500000;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("500KB");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithKiloByteStringAndNoB() {
        long expectedValue = 500000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("500 KB");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithKiloByteStringAndSpace() {
        long expectedValue = 500000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("500 KB");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithKiloByteStringAndSpaceAndNoB() {
        long expectedValue = 500000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("500 K");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithMegaByteString() {
        long expectedValue = 500000000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("500MB");
        
        assertEquals(expectedValue, valueReturn);
    }
    

    
    @Test
    public void testParseFormattedSizeToBytesWithGigaByteString() {
        long expectedValue = 5000000000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("5GB");
        
        assertEquals(expectedValue, valueReturn);
    }

    @Test
    public void testParseFormattedSizeToBytesWithTeraByteString() {
        long expectedValue = 5000000000000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("5TB");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithGigyByteString() {
        long expectedValue = 5368709120L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("5GiB");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithLowerCaseString() {
        long expectedValue = 500000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("500kb");
        
        assertEquals(expectedValue, valueReturn);
    }
    
    @Test
    public void testParseFormattedSizeToBytesWithGigaByteFullString() {
        long expectedValue = 5000000000L;
        
        long valueReturn = FileUtils.parseFormattedSizeToBytes("5 Giga");
        
        assertEquals(expectedValue, valueReturn);
    }

    @Test
    @SneakyThrows
    void testFilePermissions() {
        File tempFile = Files.createTempFile("tmp", "txt").toFile();

        Files.setPosixFilePermissions(tempFile.toPath(), PosixFilePermissions.fromString("rwxr-xr--"));

        String result = FileUtils.getPermissions(tempFile);
        assertThat(result).isEqualTo("rwxr-xr--");
    }
}
