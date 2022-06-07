package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.PermissionModel.PermissionType;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
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
@Import(MockServicesConfig.class)
public class PermissionDAOIT extends BaseDatabaseTest {

  @Autowired
  PermissionDAO dao;

  @Autowired
  RoleDAO roleDAO;

  @Autowired
  JdbcTemplate template;

  @Test
  void testWriteThenRead() {
    PermissionModel permissionModel1 = getPermissionModel1();

    PermissionModel permissionModel2 = getPermissionModel2();

    dao.save(permissionModel1);
    assertNotNull(permissionModel1.getId());
    assertEquals(1, count());

    dao.save(permissionModel2);
    assertNotNull(permissionModel2.getId());
    assertEquals(2, count());

    PermissionModel foundById1 = dao.findById(permissionModel1.getId()).get();
    assertEquals(permissionModel1.getId(), foundById1.getId());

    PermissionModel foundById2 = dao.findById(permissionModel2.getId()).get();
    assertEquals(permissionModel2.getId(), foundById2.getId());
  }

  @Test
  void testList() {
    PermissionModel permissionModel1 = getPermissionModel1();

    PermissionModel permissionModel2 = getPermissionModel2();

    dao.save(permissionModel1);
    assertNotNull(permissionModel1.getId());
    assertEquals(1, count());

    dao.save(permissionModel2);
    assertNotNull(permissionModel2.getId());
    assertEquals(2, count());

    List<PermissionModel> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(permissionModel1.getId())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(permissionModel2.getId())).count());
  }


  @Test
  void testUpdate() {
    PermissionModel permissionModel1 = getPermissionModel1();

    dao.save(permissionModel1);

    permissionModel1.setLabel("111-updated");

    dao.update(permissionModel1);

    PermissionModel found = dao.findById(permissionModel1.getId()).get();
    assertEquals(permissionModel1.getLabel(), found.getLabel());
  }

  @Test
  void testFind() {

    PermissionModel permissionModel1 = getPermissionModel1();
    PermissionModel permissionModel2 = getPermissionModel2();

    dao.save(permissionModel1);
    dao.save(permissionModel2);

    assertEquals(2, dao.count());

    PermissionModel found1 = dao.find(Permission.CAN_MANAGE_VAULTS);
    assertEquals(permissionModel1.getId(), found1.getId());

    PermissionModel found2 = dao.find(Permission.CAN_MANAGE_DEPOSITS);
    assertEquals(permissionModel2.getId(), found2.getId());

    PermissionModel found3 = dao.find(Permission.CAN_MANAGE_ARCHIVE_STORES);
    assertNull(found3);
  }

  @Test
  void testFindByType() {

    PermissionModel permissionModel1 = getPermissionModel1();
    PermissionModel permissionModel2 = getPermissionModel2();

    dao.save(permissionModel1);
    dao.save(permissionModel2);

    assertEquals(2, dao.count());

    List<PermissionModel> found1 = dao.findByType(PermissionType.VAULT);
    checkSamePermissionModelIds(found1, permissionModel1);

    List<PermissionModel> found2 = dao.findByType(PermissionType.SCHOOL);
    checkSamePermissionModelIds(found2, permissionModel2);

    List<PermissionModel> found3 = dao.findByType(PermissionType.ADMIN);
    assertTrue(found3.isEmpty());
  }

  void checkSamePermissionModelIds(Collection<PermissionModel> actual, PermissionModel... expected){
    assertEquals(
        Arrays.stream(expected).map(PermissionModel::getId).sorted().collect(Collectors.toList()),
        actual.stream().map(PermissionModel::getId).sorted().collect(Collectors.toList()));
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Role_permissions`");
    template.execute("delete from `Roles`");
    template.execute("delete from `Permissions`");
    assertEquals(0, count());
  }

  private PermissionModel getPermissionModel1() {
    PermissionModel result = new PermissionModel();
    result.setId(Permission.CAN_MANAGE_VAULTS.name());
    result.setLabel("LABEL-1");
    result.setPermission(Permission.CAN_MANAGE_VAULTS);
    result.setType(PermissionType.VAULT);
    return result;
  }

  private PermissionModel getPermissionModel2() {
    PermissionModel result = new PermissionModel();
    result.setId(Permission.CAN_MANAGE_DEPOSITS.name());
    result.setLabel("LABEL-2");
    result.setPermission(Permission.CAN_MANAGE_DEPOSITS);
    result.setType(PermissionType.SCHOOL);
    return result;
  }

  long count() {
    return dao.count();
  }


  @Test
  void testSynchronisePermissions() {
    int expected = Permission.values().length;

    assertEquals(0, dao.count());
    dao.synchronisePermissions();
    assertEquals(expected, dao.count());

    RoleModel role1 = new RoleModel();
    role1.setName("role1");
    role1.setStatus("okay");
    role1.setType(RoleType.VAULT);
    roleDAO.save(role1);

    RoleModel role2 = new RoleModel();
    role2.setName("role2");
    role2.setStatus("okay");
    role2.setType(RoleType.VAULT);
    roleDAO.save(role2);

    RoleModel role3 = new RoleModel();
    role3.setName("role3");
    role3.setStatus("okay");
    role3.setType(RoleType.VAULT);
    roleDAO.save(role3);

    //the ids on the Permission enums become the database ids for PermissionModel
    assertEquals(
        Arrays.stream(Permission.values()).map(Permission::getId).collect(Collectors.toSet()),
        dao.findAll().stream().map(PermissionModel::getId).collect(Collectors.toSet()));

    int updated1 = template.update("insert into `Permissions` (id,label,type,permission) values (?,?,?,?)","extra1","label-extra1",PermissionType.VAULT,"extra1perm");
    int updated2 = template.update("insert into `Permissions` (id,label,type,permission) values (?,?,?,?)","extra2","label-extra2",PermissionType.SCHOOL,"extra2perm");
    int updated3 = template.update("insert into `Permissions` (id,label,type,permission) values (?,?,?,?)","extra3","label-extra3",PermissionType.ADMIN,"extra3perm");
    assertEquals(1, updated1);
    assertEquals(1, updated2);
    assertEquals(1, updated3);
    assertEquals(expected+3, dao.count());

    int updated4 = template.update("insert into `Role_permissions` (role_id,permission_id) values (?,?)",role1.getId(), "extra1");
    int updated5 = template.update("insert into `Role_permissions` (role_id,permission_id) values (?,?)",role1.getId(), "extra2");
    int updated6 = template.update("insert into `Role_permissions` (role_id,permission_id) values (?,?)",role1.getId(), "extra3");

    assertEquals(1, updated4);
    assertEquals(1, updated5);
    assertEquals(1, updated6);
    assertEquals(3, roleDAO.count());

    assertEquals(3, template.queryForObject("select count(*) from `Role_permissions`",Long.class));

    int updated7 = template.update("delete from `Permissions` where id = ?",
        Permission.ASSIGN_SCHOOL_VAULT_ROLES.getId());
    assertEquals(1, updated7);

    assertEquals(expected+2, dao.count());
    dao.synchronisePermissions();

    //the extra Permissions and associated entries in Role_Permissions should have been deleted
    //and the deleted permission should be re-inserted
    assertEquals(expected, count());
    assertEquals(3, roleDAO.count());
    assertEquals(0, template.queryForObject("select count(*) from `Role_permissions`", Long.class));
    assertEquals(Permission.ASSIGN_SCHOOL_VAULT_ROLES, dao.findById(Permission.ASSIGN_SCHOOL_VAULT_ROLES.getId()).get().getPermission());
  }
}
