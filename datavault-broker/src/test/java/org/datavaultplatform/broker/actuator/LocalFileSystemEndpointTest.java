package org.datavaultplatform.broker.actuator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LocalFileSystemEndpointTest {

  @TempDir
  File tempDir;

  @Mock
  private ArchiveStoreService mArchiveStoreService;

  @InjectMocks
  private LocalFileStoreEndpoint endpoint;

  @Test
  void testNoLocalFileStores1() {
    when(mArchiveStoreService.getArchiveStores()).thenReturn(Collections.emptyList());
    List<LocalFileStoreInfo> result = endpoint.getLocalFileStoresInfo();
    assertTrue(result.isEmpty());
    Mockito.verify(mArchiveStoreService).getArchiveStores();
    Mockito.verifyNoMoreInteractions(mArchiveStoreService);
  }

  @Test
  void testNoLocalFileStores2() {
    ArchiveStore as1 = new ArchiveStore();
     when(mArchiveStoreService.getArchiveStores()).thenReturn(Arrays.asList(as1));
    List<LocalFileStoreInfo> result = endpoint.getLocalFileStoresInfo();
    assertTrue(result.isEmpty());
    Mockito.verify(mArchiveStoreService).getArchiveStores();
    Mockito.verifyNoMoreInteractions(mArchiveStoreService);
  }

  @Test
  void testSparseLocalFileStores() {
    ArchiveStore as1 = new ArchiveStore();
    as1.setStorageClass(LocalFileSystem.class.getName());
    when(mArchiveStoreService.getArchiveStores()).thenReturn(Arrays.asList(as1));
    List<LocalFileStoreInfo> result = endpoint.getLocalFileStoresInfo();
    assertEquals(1,result.size());

    LocalFileStoreInfo info = result.get(0);
    assertEquals(null, info.getId());
    assertEquals(null, info.getLabel());
    assertEquals(false, info.isValid());
    assertEquals(false, info.isRetrieveEnabled());
    assertTrue(info.getValidationException().contains("IllegalArgumentException"));
    assertTrue(info.getValidationException().contains("cannot be null"));
    Mockito.verify(mArchiveStoreService).getArchiveStores();
    Mockito.verifyNoMoreInteractions(mArchiveStoreService);
  }

  @Test
  @SneakyThrows
  void testNonSparseLocalFileStores() {
    ArchiveStore as1 = new ArchiveStore();
    as1.setRetrieveEnabled(true);
    as1.getProperties().put(LocalFileSystem.ROOT_PATH, tempDir.getAbsolutePath());
    as1.setStorageClass(LocalFileSystem.class.getName());
    as1.setLabel("test-label");
    Field fId = ArchiveStore.class.getDeclaredField("id");
    fId.setAccessible(true);
    fId.set(as1, "test-id");
    when(mArchiveStoreService.getArchiveStores()).thenReturn(Arrays.asList(as1));
    List<LocalFileStoreInfo> result = endpoint.getLocalFileStoresInfo();
    assertEquals(1,result.size());

    LocalFileStoreInfo info = result.get(0);
    assertEquals("test-id", info.getId());
    assertEquals("test-label", info.getLabel());
    assertEquals(true, info.isValid());
    assertEquals(true, info.isRetrieveEnabled());
    assertNull(info.getValidationException());
    Mockito.verify(mArchiveStoreService).getArchiveStores();
    Mockito.verifyNoMoreInteractions(mArchiveStoreService);
  }
}
