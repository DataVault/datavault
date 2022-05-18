package org.datavaultplatform.broker.authentication;

import static org.datavaultplatform.broker.authentication.AuthTestData.GROUP_1;
import static org.datavaultplatform.broker.authentication.AuthTestData.GROUP_2;
import static org.datavaultplatform.broker.authentication.AuthTestData.VAULT_INFO_1;
import static org.datavaultplatform.broker.authentication.AuthTestData.VAULT_INFO_2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.Arrays;
import org.datavaultplatform.broker.controllers.GroupsController;
import org.datavaultplatform.common.model.Group;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class GroupsControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  GroupsController controller;

  @Captor
  ArgumentCaptor<Group> argGroup;

  @Captor
  ArgumentCaptor<String> argUserId;

  @Test
  void testPostAddGroup() throws Exception {
    when(controller.addGroup(argUserId.capture(), argGroup.capture())).thenReturn(
        AuthTestData.GROUP_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/groups")
            .content(mapper.writeValueAsString(AuthTestData.GROUP_1))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.GROUP_1);

    verify(controller).addGroup(argUserId.getValue(), argGroup.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals(AuthTestData.GROUP_1.getName(), argGroup.getValue().getName());
  }

  @Test
  void testPutAddGroupOwner() throws Exception {
    doNothing().when(controller).addGroupOwner(USER_ID_1, "group-id-one", "owner-user-id-xxx");

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/groups/{groupid}/users/{owneruserid}", "group-id-one", "owner-user-id-xxx"),
        null);

    verify(controller).addGroupOwner(USER_ID_1, "group-id-one", "owner-user-id-xxx");
  }

  @Test
  void testDeleteGroup() {
    when(controller.deleteGroup(USER_ID_1, "group-id-xxx")).thenReturn(true);

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/groups/{groupid}", "group-id-xxx"),
        true);

    verify(controller).deleteGroup(USER_ID_1, "group-id-xxx");
  }

  @Test
  void testPutDisableGroup() throws Exception {
    doNothing().when(controller).disableGroup(USER_ID_1, "group-id-yyy");

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/groups/{groupid}/disable", "group-id-yyy"),
        null);

    verify(controller).disableGroup(USER_ID_1, "group-id-yyy");
  }

  @Test
  void testPutEnableGroup() throws Exception {
    doNothing().when(controller).enableGroup(USER_ID_1, "group-id-zzz");

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/groups/{groupid}/enable", "group-id-zzz"),
        null);

    verify(controller).enableGroup(USER_ID_1, "group-id-zzz");
  }

  @Test
  void testGetGroup() {
    when(controller.getGroup(USER_ID_1, "group-id-zzz")).thenReturn(AuthTestData.GROUP_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/groups/{groupid}", "group-id-zzz"),
        AuthTestData.GROUP_1);

    verify(controller).getGroup(USER_ID_1, "group-id-zzz");
  }

  @Test
  void testGetGroupVaultsCount() {
    when(controller.getGroupVaultCount(USER_ID_1, "group-id-abcde")).thenReturn(123);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/groups/{groupid}/count", "group-id-abcde"),
        123);

    verify(controller).getGroupVaultCount(USER_ID_1, "group-id-abcde");
  }

  @Test
  void testGetGroupVaults() {
    when(controller.getGroupVaults(USER_ID_1, "group-id-abc")).thenReturn(
        Arrays.asList(VAULT_INFO_1, VAULT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/groups/{groupid}/vaults", "group-id-abc"),
        Arrays.asList(VAULT_INFO_1, VAULT_INFO_2));

    verify(controller).getGroupVaults(USER_ID_1, "group-id-abc");
  }

  @Test
  void testGetGroups() {
    when(controller.getGroups(USER_ID_1)).thenReturn(Arrays.asList(GROUP_1, GROUP_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/groups"),
        Arrays.asList(GROUP_1, GROUP_2));

    verify(controller).getGroups(USER_ID_1);
  }

  @Test
  void testGetGroupsByScopedPermissions() {
    when(controller.getGroupsByScopedPermissions(USER_ID_1)).thenReturn(
        Arrays.asList(GROUP_1, GROUP_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/groups/byScopedPermissions"),
        Arrays.asList(GROUP_1, GROUP_2));

    verify(controller).getGroupsByScopedPermissions(USER_ID_1);
  }

  @Test
  void testGetGroupCount() {
    when(controller.getGroupsCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/groups/count"),
        1234);

    verify(controller).getGroupsCount(USER_ID_1);
  }


  @Test
  void testDeleteRemoveGroupOwner() throws Exception {
    doNothing().when(controller).removeGroupOwner(USER_ID_1, "group1", "ownerUserId1");

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/groups/{groupid}/users/{owneruserid}", "group1", "ownerUserId1"),
        null);

    verify(controller).removeGroupOwner(USER_ID_1, "group1", "ownerUserId1");
  }

  @Test
  void testPostUpdateGroup() throws Exception {
    when(controller.updateGroup(argUserId.capture(), argGroup.capture())).thenReturn(
        ResponseEntity.ok(GROUP_1));

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/groups/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(GROUP_1)),
        GROUP_1);

    verify(controller).updateGroup(argUserId.getValue(), argGroup.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals(GROUP_1.getID(), argGroup.getValue().getID());
  }
}