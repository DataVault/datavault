package org.datavaultplatform.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic;
import org.datavaultplatform.common.storage.impl.S3Cloud;
import org.datavaultplatform.common.storage.impl.SFTPFileSystem;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
public class DataStoreTest {

  static Stream<Arguments> getClassAndTypePairs() {
    return Stream.of (
            S3Cloud.class,
            SFTPFileSystem.class,
            LocalFileSystem.class,
            TivoliStorageManager.class,
            OracleObjectStorageClassic.class
        )
        .map(Class::getName)
        .map(className -> Arguments.of(className.substring(className.lastIndexOf(".") + 1),
            className)
        );
  }

  @ParameterizedTest
  @MethodSource("getClassAndTypePairs")
  void testValidTypes(String type, String expectedClassName) {
    String className = StorageConstants.getStorageClass(type).get();
    assertEquals(expectedClassName, className);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "\t", "s3", "usb"})
  @NullSource
  void testInvalidTypes(String invalidType) {
    assertFalse(StorageConstants.getStorageClass(invalidType).isPresent());
  }
}
