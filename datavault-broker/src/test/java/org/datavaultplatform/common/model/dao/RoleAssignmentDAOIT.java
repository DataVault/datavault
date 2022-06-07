package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.PermissionModel.PermissionType;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=false",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
public class RoleAssignmentDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  RoleAssignmentDAO dao;

  @Autowired
  RoleDAO roleDAO;

  @Autowired
  VaultDAO vaultDAO;

  @Autowired
  PermissionDAO permissionDAO;

  private RoleModel role1;
  private RoleModel role2;

  private RoleModel role3isAdmimRole;

  @Test
  void testWriteThenRead() {
    RoleAssignment roleAssignment1 = getRoleAssignment1();

    RoleAssignment roleAssignment2 = getRoleAssignment2();

    dao.save(roleAssignment1);
    assertNotNull(roleAssignment1.getId());
    assertEquals(1, count());

    dao.save(roleAssignment2);
    assertNotNull(roleAssignment2.getId());
    assertEquals(2, count());

    RoleAssignment foundById1 = dao.findById(roleAssignment1.getId()).get();
    assertEquals(roleAssignment1.getId(), foundById1.getId());

    RoleAssignment foundById2 = dao.findById(roleAssignment2.getId()).get();
    assertEquals(roleAssignment2.getId(), foundById2.getId());
  }

  @Test
  void testList() {
    RoleAssignment ra1 = getRoleAssignment1();

    RoleAssignment ra2 = getRoleAssignment2();

    dao.save(ra1);
    assertEquals(1, dao.count());

    dao.save(ra2);
    assertEquals(2, dao.count());

    List<RoleAssignment> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1,items.stream().filter(dr -> dr.getId().equals(ra1.getId())).count());
    assertEquals(1,items.stream().filter(dr -> dr.getId().equals(ra2.getId())).count());

  }

  @Test
  void testUpdate() {
    RoleAssignment roleAssignment1 = getRoleAssignment1();

    dao.save(roleAssignment1);

    roleAssignment1.setUserId("updated-user-id");

    dao.update(roleAssignment1);

    RoleAssignment found = dao.findById(roleAssignment1.getId()).get();
    assertEquals(roleAssignment1.getUserId(), found.getUserId());
  }

  @Test
  void testByPendingVaultId() {

      assertTrue(dao.findByPendingVaultId(null).isEmpty());
      assertTrue(dao.findByPendingVaultId("").isEmpty());
      assertTrue(dao.findByPendingVaultId("XXX").isEmpty());

      RoleAssignment ra1 = getRoleAssignment1();
      RoleAssignment ra2 = getRoleAssignment2();
      RoleAssignment ra3 = getRoleAssignment3();
      RoleAssignment ra4 = getRoleAssignment4();
      assertNull(ra1.getPendingVaultId());
      assertEquals("pv1",ra2.getPendingVaultId());
      assertEquals("pv1",ra3.getPendingVaultId());
      assertEquals("pv2",ra4.getPendingVaultId());


      dao.save(ra1);
      dao.save(ra2);
      dao.save(ra3);
      dao.save(ra4);
      assertEquals(4,dao.count());

      assertTrue(dao.findByPendingVaultId("pv0").isEmpty());

      assertEquals(
          Arrays.asList(ra2.getId(),ra3.getId()),
          dao.findByPendingVaultId("pv1").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

      assertEquals(
        Arrays.asList(ra4.getId()),
        dao.findByPendingVaultId("pv2").stream().map(RoleAssignment::getId).collect(Collectors.toList()));
  }

  @Test
  void testByRoleId() {

    assertTrue(dao.findByRoleId(null).isEmpty());
    assertTrue(dao.findByRoleId(-1L).isEmpty());
    assertTrue(dao.findByRoleId(0L).isEmpty());

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();
    assertEquals(role1, ra1.getRole());
    assertEquals(role2, ra2.getRole());
    assertEquals(role2, ra3.getRole());
    assertEquals(role1, ra4.getRole());


    dao.save(ra1);
    dao.save(ra2);
    dao.save(ra3);
    dao.save(ra4);
    assertEquals(4,dao.count());

    assertEquals(
        Arrays.asList(ra1.getId(), ra4.getId()),
        dao.findByRoleId(role1.getId()).stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra2.getId(), ra3.getId()),
        dao.findByRoleId(role2.getId()).stream().map(RoleAssignment::getId).collect(Collectors.toList()));
  }

  @Test
  void testBySchoolId() {

    assertTrue(dao.findBySchoolId(null).isEmpty());
    assertTrue(dao.findBySchoolId("").isEmpty());
    assertTrue(dao.findBySchoolId("xxx").isEmpty());

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();

    assertEquals("school-one", ra1.getSchoolId());
    assertEquals("school-two", ra2.getSchoolId());
    assertEquals("school-two", ra3.getSchoolId());
    assertEquals("school-one", ra4.getSchoolId());

    dao.save(ra1);
    dao.save(ra2);
    dao.save(ra3);
    dao.save(ra4);
    assertEquals(4,dao.count());

    assertEquals(
        Arrays.asList(ra1.getId(), ra4.getId()),
        dao.findBySchoolId("school-one").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra2.getId(), ra3.getId()),
        dao.findBySchoolId("school-two").stream().map(RoleAssignment::getId).collect(Collectors.toList()));
  }

  @Test
  void testByUserId() {

    assertTrue(dao.findByUserId(null).isEmpty());
    assertTrue(dao.findByUserId("").isEmpty());
    assertTrue(dao.findByUserId("xxx").isEmpty());

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();
    RoleAssignment ra5 = getRoleAssignment5();

    dao.save(ra1);
    dao.save(ra2);
    dao.save(ra3);
    dao.save(ra4);
    dao.save(ra5);
    assertEquals(5, dao.count());

    assertEquals(
        Arrays.asList(ra1.getId(), ra5.getId()),
        dao.findByUserId("user-id-1").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra2.getId()),
        dao.findByUserId("user-id-2").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra3.getId()),
        dao.findByUserId("user-id-3").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra4.getId()),
        dao.findByUserId("user-id-4").stream().map(RoleAssignment::getId).collect(Collectors.toList()));
  }

  @Test
  void testByVaultId() {

    assertTrue(dao.findByVaultId(null).isEmpty());
    assertTrue(dao.findByVaultId("").isEmpty());
    assertTrue(dao.findByVaultId("xxx").isEmpty());

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();
    RoleAssignment ra5 = getRoleAssignment5();

    dao.save(ra1);
    dao.save(ra2);
    dao.save(ra3);
    dao.save(ra4);
    dao.save(ra5);
    assertEquals(5, dao.count());

    assertEquals(
        Arrays.asList(ra1.getId()),
        dao.findByVaultId("vault-id-one").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra2.getId()),
        dao.findByVaultId("vault-id-two").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra3.getId()),
        dao.findByVaultId("vault-id-three").stream().map(RoleAssignment::getId).collect(Collectors.toList()));

    assertEquals(
        Arrays.asList(ra4.getId(), ra5.getId()),
        dao.findByVaultId("vault-id-four").stream().map(RoleAssignment::getId).collect(Collectors.toList()));
  }

  @Test
  void testIsAdminUser() {

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();
    RoleAssignment ra5 = getRoleAssignment5();
    RoleAssignment ra6 = getRoleAssignment6ForIsAdmin();

    dao.save(ra1);
    dao.save(ra2);
    dao.save(ra3);
    dao.save(ra4);
    dao.save(ra5);
    dao.save(ra6);

    assertFalse(dao.isAdminUser(ra1.getUserId()));
    assertFalse(dao.isAdminUser(ra1.getUserId()));
    assertFalse(dao.isAdminUser(ra1.getUserId()));
    assertFalse(dao.isAdminUser(ra1.getUserId()));
    assertFalse(dao.isAdminUser(ra1.getUserId()));
    assertTrue(dao.isAdminUser(ra6.getUserId()));
  }

  @Test
  void testFindUserPermissions() {

    assertTrue(dao.findUserPermissions(null).isEmpty());
    assertTrue(dao.findUserPermissions("").isEmpty());
    assertTrue(dao.findUserPermissions("xxx").isEmpty());

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();

    assertEquals(role1, ra1.getRole());
    assertEquals("user-id-1", ra1.getUserId());

    assertEquals(role2, ra2.getRole());
    assertEquals("user-id-2", ra2.getUserId());

    assertEquals(role2, ra3.getRole());
    assertEquals("user-id-3", ra3.getUserId());

    assertEquals(role1, ra4.getRole());
    assertEquals("user-id-4", ra4.getUserId());

    dao.save(ra1);
    dao.save(ra2);
    dao.save(ra3);
    dao.save(ra4);
    assertEquals(4, dao.count());

    assertEquals(
        getPermissionNamesForRole(role1),
        getPermissionNames(dao.findUserPermissions("user-id-1")));

    assertEquals(
        getPermissionNamesForRole(role2),
        getPermissionNames(dao.findUserPermissions("user-id-2")));

    assertEquals(
        getPermissionNamesForRole(role2),
        getPermissionNames(dao.findUserPermissions("user-id-3")));

    assertEquals(
        getPermissionNamesForRole(role1),
        getPermissionNames(dao.findUserPermissions("user-id-4")));
  }

  @Test
  void testHasPermission() {

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();

    assertEquals(role1, ra1.getRole());
    assertEquals("user-id-1", ra1.getUserId());

    assertEquals(role2, ra2.getRole());
    assertEquals("user-id-2", ra2.getUserId());

    assertEquals(role2, ra3.getRole());
    assertEquals("user-id-3", ra3.getUserId());

    assertEquals(role1, ra4.getRole());
    assertEquals("user-id-4", ra4.getUserId());

    dao.save(ra1);
    dao.save(ra2);
    dao.save(ra3);
    dao.save(ra4);
    assertEquals(4, dao.count());

    List<Permission> permissionsToCheck = Collections.unmodifiableList(Arrays.asList(
        Permission.CAN_MANAGE_VAULTS,
        Permission.CAN_MANAGE_DEPOSITS,
        Permission.CAN_VIEW_EVENTS,
        Permission.CAN_VIEW_QUEUES));

      //check permissions from role1
      checkUserHasPermission(permissionsToCheck, "user-id-1", Permission.CAN_MANAGE_VAULTS, Permission.CAN_MANAGE_DEPOSITS);
      checkUserHasPermission(permissionsToCheck, "user-id-4", Permission.CAN_MANAGE_VAULTS, Permission.CAN_MANAGE_DEPOSITS);
      //check permissions from role2
      checkUserHasPermission(permissionsToCheck, "user-id-2", Permission.CAN_VIEW_EVENTS, Permission.CAN_VIEW_QUEUES);
      checkUserHasPermission(permissionsToCheck, "user-id-3", Permission.CAN_VIEW_EVENTS, Permission.CAN_VIEW_QUEUES);
  }

  @Test
  void testRoleAssignmentExists() {

    RoleAssignment ra1 = getRoleAssignment1();
    RoleAssignment ra2 = getRoleAssignment2();
    RoleAssignment ra3 = getRoleAssignment3();
    RoleAssignment ra4 = getRoleAssignment4();

    dao.saveAll(Arrays.asList(ra1,ra3));

    assertTrue(dao.roleAssignmentExists(ra1));
    assertFalse(dao.roleAssignmentExists(ra2));
    assertTrue(dao.roleAssignmentExists(ra3));
    assertFalse(dao.roleAssignmentExists(ra4));
  }

  private void checkUserHasPermission(List<Permission> permissionsToCheck, String userId, Permission... expectedUserPermissions) {

    //check we don't have permissions we're NOT meant to have
    List<Permission> doesNotHavePermissions = new ArrayList<>(permissionsToCheck);
    Arrays.stream(expectedUserPermissions).forEach( expectedPermission ->
        doesNotHavePermissions.remove(expectedPermission)
    );
    doesNotHavePermissions.forEach( doesNotHavePermission ->
        assertFalse(dao.hasPermission(userId, doesNotHavePermission))
    );

    //check we DO have permissions we're meant to have
    Arrays.stream(expectedUserPermissions).forEach( expectedPermission ->
        assertTrue(dao.hasPermission(userId, expectedPermission))
    );
  }

  private Set<String> getPermissionNames(Collection<Permission> permissions){
    return permissions.stream()
        .map(Permission::getRoleName)
        .collect(Collectors.toSet());
  }

  private Set<String> getPermissionNamesForRole(RoleModel role){
    return role.getPermissions().stream()
        .map(PermissionModel::getPermission)
        .map(Permission::getRoleName)
        .collect(Collectors.toSet());
  }


  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Role_permissions`");
    template.execute("delete from `Role_assignments`");
    template.execute("delete from `Roles`");
    assertEquals(0, count());
  }

  private RoleAssignment getRoleAssignment1() {
    RoleAssignment result = new RoleAssignment();
    result.setUserId("user-id-1");
    result.setRole(role1);
    result.setSchoolId("school-one");
    result.setVaultId("vault-id-one");
    return result;
  }

  private RoleAssignment getRoleAssignment2() {
    RoleAssignment result = new RoleAssignment();
    result.setUserId("user-id-2");
    result.setRole(role2);
    result.setPendingVaultId("pv1");
    result.setSchoolId("school-two");
    result.setVaultId("vault-id-two");
    return result;
  }

  private RoleAssignment getRoleAssignment3() {
    RoleAssignment result = new RoleAssignment();
    result.setUserId("user-id-3");
    result.setRole(role2);
    result.setPendingVaultId("pv1");
    result.setSchoolId("school-two");
    result.setVaultId("vault-id-three");
    return result;
  }


  private RoleAssignment getRoleAssignment4() {
    RoleAssignment result = new RoleAssignment();
    result.setUserId("user-id-4");
    result.setRole(role1);
    result.setPendingVaultId("pv2");
    result.setSchoolId("school-one");
    result.setVaultId("vault-id-four");
    return result;
  }

  private RoleAssignment getRoleAssignment5() {
    RoleAssignment result = new RoleAssignment();
    result.setUserId("user-id-1");
    result.setRole(role1);
    result.setPendingVaultId("pv3");
    result.setSchoolId("school-three");
    result.setVaultId("vault-id-four");
    return result;
  }


  private RoleAssignment getRoleAssignment6ForIsAdmin() {
    RoleAssignment result = new RoleAssignment();
    result.setUserId("user-id-A");
    result.setRole(role3isAdmimRole);
    result.setPendingVaultId("pv5");
    result.setSchoolId("school-three");
    result.setVaultId("vault-id-five");
    return result;
  }

  long count() {
     return dao.count();
  }

  @BeforeEach
  void beforeEach() {
    this.role1 = roleDAO.save(RoleDAOIT.getRoleModel1());
    this.role2 = roleDAO.save(RoleDAOIT.getRoleModel2());
    this.role3isAdmimRole = roleDAO.save(RoleDAOIT.getRoleModel3isAdminRoleName());

    PermissionModel pm1 = getPm1();
    PermissionModel pm2 = getPm2();
    PermissionModel pm3 = getPm3();
    PermissionModel pm4 = getPm4();
    permissionDAO.saveAll(Arrays.asList(pm1, pm2, pm3, pm4));

    role1.setPermissions(Arrays.asList(pm1, pm4));
    roleDAO.update(role1);

    role2.setPermissions(Arrays.asList(pm2, pm3));
    roleDAO.update(role2);
  }


  private static PermissionModel getPm1(){
    PermissionModel pm1 = new PermissionModel();
    pm1.setId(Permission.CAN_MANAGE_VAULTS.name());
    pm1.setPermission(Permission.CAN_MANAGE_VAULTS);
    pm1.setLabel("label1");
    pm1.setType(PermissionType.VAULT);
    return pm1;
  }
  private static PermissionModel getPm2(){
    PermissionModel pm2 = new PermissionModel();
    pm2.setId(Permission.CAN_VIEW_EVENTS.name());
    pm2.setPermission(Permission.CAN_VIEW_EVENTS);
    pm2.setLabel("label2");
    pm2.setType(PermissionType.SCHOOL);
    return pm2;
  }
  private static PermissionModel getPm3(){
    PermissionModel pm3 = new PermissionModel();
    pm3.setId(Permission.CAN_VIEW_QUEUES.name());
    pm3.setPermission(Permission.CAN_VIEW_QUEUES);
    pm3.setLabel("label3");
    pm3.setType(PermissionType.SCHOOL);
    return pm3;
  }
  private static PermissionModel getPm4(){
    PermissionModel pm4 = new PermissionModel();
    pm4.setId(Permission.CAN_MANAGE_DEPOSITS.name());
    pm4.setPermission(Permission.CAN_MANAGE_DEPOSITS);
    pm4.setLabel("label4");
    pm4.setType(PermissionType.VAULT);
    return pm4;
  }

}
