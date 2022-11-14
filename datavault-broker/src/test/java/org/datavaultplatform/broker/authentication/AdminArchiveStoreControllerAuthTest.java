package org.datavaultplatform.broker.authentication;

import static org.datavaultplatform.broker.authentication.AuthTestData.ARCHIVE_STORE_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
import org.datavaultplatform.broker.controllers.admin.AdminArchiveStoreController;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class AdminArchiveStoreControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  AdminArchiveStoreController controller;
  @Captor
  ArgumentCaptor<ArchiveStore> argArchiveStore;
  @Captor
  ArgumentCaptor<String> argUserId;

  @Test
  void testGetArchiveStores() {

    ResponseEntity<List<ArchiveStore>> result = ResponseEntity.ok(
        Arrays.asList(ARCHIVE_STORE_1, AuthTestData.ARCHIVE_STORE_2));

    when(controller.getArchiveStores(USER_ID_1)).thenReturn(result);

    checkWorksWhenAuthenticatedFailsOtherwise(get("/admin/archivestores"), Arrays.asList(
            ARCHIVE_STORE_1, AuthTestData.ARCHIVE_STORE_2),
        Permission.CAN_MANAGE_ARCHIVE_STORES);

    verify(controller).getArchiveStores(USER_ID_1);
  }

  @Test
  void testGetArchiveStore() {

    ResponseEntity<ArchiveStore> result = ResponseEntity.ok(ARCHIVE_STORE_1);
    when(controller.getArchiveStore(USER_ID_1, "2112")).thenReturn(result);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/archivestores/{archivestoreid}", "2112"),
        ARCHIVE_STORE_1,
        Permission.CAN_MANAGE_ARCHIVE_STORES);

    verify(controller).getArchiveStore(USER_ID_1, "2112");
  }

  @Test
  void testPostArchiveStores() throws JsonProcessingException {
    ResponseEntity<ArchiveStore> result = ResponseEntity.ok(AuthTestData.ARCHIVE_STORE_2);
    when(controller.addArchiveStore(argUserId.capture(), argArchiveStore.capture())).thenReturn(
        result);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/admin/archivestores")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(ARCHIVE_STORE_1)),
        AuthTestData.ARCHIVE_STORE_2,
        Permission.CAN_MANAGE_ARCHIVE_STORES);

    verify(controller).addArchiveStore(argUserId.getValue(), argArchiveStore.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals(ARCHIVE_STORE_1.getID(), argArchiveStore.getValue().getID());
  }

  @Test
  void testPutArchiveStores() throws JsonProcessingException {
    ResponseEntity<ArchiveStore> result = ResponseEntity.ok(AuthTestData.ARCHIVE_STORE_2);
    when(controller.editArchiveStore(argUserId.capture(), argArchiveStore.capture())).thenReturn(
        result);

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/admin/archivestores")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(ARCHIVE_STORE_1)),
        AuthTestData.ARCHIVE_STORE_2,
        Permission.CAN_MANAGE_ARCHIVE_STORES);

    verify(controller).editArchiveStore(argUserId.getValue(), argArchiveStore.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals(ARCHIVE_STORE_1.getID(), argArchiveStore.getValue().getID());
  }

  @Test
  void testDeleteArchiveStore() {
    when(controller.deleteArchiveStore(USER_ID_1, "2112")).thenReturn(
        ResponseEntity.ok().body(null));

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/admin/archivestores/{archivesstoreid}", "2112"),
        null,
        Permission.CAN_MANAGE_ARCHIVE_STORES);

    verify(controller).deleteArchiveStore(USER_ID_1, "2112");
  }

  @AfterEach
  void checkSecurityRoles() {
    checkHasSecurityAdminArchiveStoresRole();
  }
}
