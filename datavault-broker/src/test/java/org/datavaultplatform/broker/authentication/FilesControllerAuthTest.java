package org.datavaultplatform.broker.authentication;


import static org.datavaultplatform.broker.authentication.AuthTestData.FILE_INFO_1;
import static org.datavaultplatform.broker.authentication.AuthTestData.FILE_INFO_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import org.datavaultplatform.broker.controllers.FilesController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;

public class FilesControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  FilesController controller;

  @Captor
  ArgumentCaptor<String> argUserId;
  @Captor
  ArgumentCaptor<String> argStorageId;

  @Captor
  ArgumentCaptor<String> argFilename;

  @Captor
  ArgumentCaptor<String> argFileUploadHandle;

  @Captor
  ArgumentCaptor<HttpServletRequest> argRequest;

  @Test
  void testGetCheckDepositSize() throws Exception {

    when(controller.checkDepositSize(argUserId.capture(), argRequest.capture())).thenReturn(
        AuthTestData.DEPOSIT_SIZE);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/checkdepositsize"),
        AuthTestData.DEPOSIT_SIZE);

    verify(controller).checkDepositSize(argUserId.getValue(), argRequest.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
  }


  @Test
  void testGetFilesListing() throws Exception {

    when(controller.getFilesListing(argUserId.capture(), argRequest.capture(),
        argStorageId.capture())).thenReturn(
        Arrays.asList(AuthTestData.FILE_INFO_1, AuthTestData.FILE_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/files/{storageid}", "storage-id-xxx"),
        Arrays.asList(AuthTestData.FILE_INFO_1, AuthTestData.FILE_INFO_2));

    verify(controller).getFilesListing(argUserId.getValue(), argRequest.getValue(),
        argStorageId.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals("storage-id-xxx", argStorageId.getValue());
  }

  @Test
  void testGetFileSize() throws Exception {

    when(controller.getFilesize(argUserId.capture(), argRequest.capture(),
        argStorageId.capture())).thenReturn("file-size");

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/filesize/{storageid}/**", "storage-id-xxx"),
        "file-size");

    verify(controller).getFilesize(argUserId.getValue(), argRequest.getValue(),
        argStorageId.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals("storage-id-xxx", argStorageId.getValue());
  }

  @Test
  void testGetStorageListing() {

    when(controller.getStorageListing(argUserId.capture(), argRequest.capture())).thenReturn(
        Arrays.asList(FILE_INFO_1, FILE_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/files"),
        Arrays.asList(FILE_INFO_1, FILE_INFO_2));

    verify(controller).getStorageListing(argUserId.getValue(), argRequest.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
  }

  @Test
  void testPostFileChunk() throws Exception {

    when(controller.postFileChunk(argUserId.capture(), argRequest.capture(),
        argFileUploadHandle.capture(), argFilename.capture()))
        .thenReturn("some-result-string");

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/upload/{fileUploadHandle}/{filename}", "fuh-one", "filename1"),
        "some-result-string");

    verify(controller).postFileChunk(argUserId.capture(), argRequest.capture(),
        argFileUploadHandle.capture(), argFilename.capture());

    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals("filename1", argFilename.getValue());
    assertEquals("fuh-one", argFileUploadHandle.getValue());
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }

}
