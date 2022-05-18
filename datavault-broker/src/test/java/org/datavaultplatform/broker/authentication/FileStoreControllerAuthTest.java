package org.datavaultplatform.broker.authentication;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import org.datavaultplatform.broker.controllers.FileStoreController;
import org.datavaultplatform.common.model.FileStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class FileStoreControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  FileStoreController controller;

  @Captor
  ArgumentCaptor<String> argUserId;

  @Captor
  ArgumentCaptor<FileStore> argFileStore;

  @Test
  void testPostAddFileStore() throws Exception {
    when(controller.addFileStore(argUserId.capture(), argFileStore.capture())).thenReturn(
        new ResponseEntity<>(
            AuthTestData.FILESTORE_1, HttpStatus.CREATED));

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/filestores")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(AuthTestData.FILESTORE_1)),
        AuthTestData.FILESTORE_1,
        HttpStatus.CREATED,
        false);

    verify(controller).addFileStore(argUserId.getValue(), argFileStore.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals(AuthTestData.FILESTORE_1.getLabel(), argFileStore.getValue().getLabel());
  }

  @Test
  void testPostAddFileStoreSFTP() throws JsonProcessingException {
    when(controller.addFileStoreSFTP(argUserId.capture(), argFileStore.capture())).thenReturn(
        new ResponseEntity<>(
            AuthTestData.FILESTORE_1, HttpStatus.CREATED));

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/filestores/sftp")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(AuthTestData.FILESTORE_1)),
        AuthTestData.FILESTORE_1,
        HttpStatus.CREATED,
        false);

    verify(controller).addFileStoreSFTP(argUserId.getValue(), argFileStore.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals(AuthTestData.FILESTORE_1.getLabel(), argFileStore.getValue().getLabel());
  }

  @Test
  void testDeleteFileStore() {
    when(controller.deleteFileStore(USER_ID_1, "file-store-id-xxx")).thenReturn(
        ResponseEntity.noContent().build());

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/filestores/{filestoreid}", "file-store-id-xxx"),
        null,
        HttpStatus.NO_CONTENT,
        false);

    verify(controller).deleteFileStore(USER_ID_1, "file-store-id-xxx");
  }

  @Test
  void testGetFileStore() {
    when(controller.getFileStore(USER_ID_1, "file-store-id-xxx")).thenReturn(
        ResponseEntity.ok(AuthTestData.FILESTORE_1));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/filestores/{filestoreid}", "file-store-id-xxx"),
        AuthTestData.FILESTORE_1);

    verify(controller).getFileStore(USER_ID_1, "file-store-id-xxx");
  }

  @Test
  void testGetFileStores() {
    when(controller.getFileStores(USER_ID_1)).thenReturn(
        ResponseEntity.ok(Arrays.asList(AuthTestData.FILESTORE_1, AuthTestData.FILESTORE_2)));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/filestores"),
        Arrays.asList(AuthTestData.FILESTORE_1, AuthTestData.FILESTORE_2));

    verify(controller).getFileStores(USER_ID_1);
  }

  @Test
  void testGetFileStoresLocal() {
    when(controller.getFileStoresLocal(USER_ID_1)).thenReturn(
        ResponseEntity.ok(Arrays.asList(AuthTestData.FILESTORE_1, AuthTestData.FILESTORE_2)));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/filestores/local"),
        Arrays.asList(AuthTestData.FILESTORE_1, AuthTestData.FILESTORE_2));

    verify(controller).getFileStoresLocal(USER_ID_1);
  }

  @Test
  void testGetFileStoresSFTP() {
    when(controller.getFileStoresSFTP(USER_ID_1)).thenReturn(
        ResponseEntity.ok(Arrays.asList(AuthTestData.FILESTORE_1, AuthTestData.FILESTORE_2)));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/filestores/sftp"),
        Arrays.asList(AuthTestData.FILESTORE_1, AuthTestData.FILESTORE_2));

    verify(controller).getFileStoresSFTP(USER_ID_1);
  }

  @Test
  void testGetFileStoreSFTP() {
    when(controller.getFilestoreSFTP(USER_ID_1, "file-store-id")).thenReturn(
        ResponseEntity.ok(AuthTestData.FILESTORE_1));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/filestores/sftp/{filestoreid}", "file-store-id"),
        AuthTestData.FILESTORE_1);

    verify(controller).getFilestoreSFTP(USER_ID_1, "file-store-id");
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }

}
