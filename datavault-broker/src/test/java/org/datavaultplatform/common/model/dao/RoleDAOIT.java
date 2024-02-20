package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.util.RoleUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.initialise.enabled=false",
    "broker.scheduled.enabled=false"
})
public class RoleDAOIT extends BaseDatabaseTest {

  @Autowired
  RoleDAO dao;

  @Autowired
  JdbcTemplate template;

  @PersistenceContext
  EntityManager em;

  @Autowired
  UserDAO userDAO;

  @Autowired
  RoleAssignmentDAO roleAssignmentDAO;

  @Autowired
  PermissionDAO permissionDAO;

  @Test
  void testWriteThenRead() {
    RoleModel RoleModel1 = getRoleModel1();

    RoleModel RoleModel2 = getRoleModel2();

    dao.save(RoleModel1);
    assertNotNull(RoleModel1.getId());
    assertEquals(1, dao.count());

    dao.save(RoleModel2);
    assertNotNull(RoleModel2.getId());
    assertEquals(2, dao.count());

    RoleModel foundById1 = dao.findById(RoleModel1.getId()).get();
    assertEquals(RoleModel1.getName(), foundById1.getName());

    RoleModel foundById2 = dao.findById(RoleModel2.getId()).get();
    assertEquals(RoleModel2.getName(), foundById2.getName());
  }

  @Test
  void testList() {
    RoleModel roleModel1 = getRoleModel1();

    RoleModel roleModel2 = getRoleModel2();

    dao.save(roleModel1);
    assertEquals(1, dao.count());

    dao.save(roleModel2);
    assertEquals(2, dao.count());

    List<RoleModel> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1,items.stream().filter(dr -> dr.getId().equals(roleModel1.getId())).count());
    assertEquals(1,items.stream().filter(dr -> dr.getId().equals(roleModel2.getId())).count());
  }


  @Test
  void testUpdate() {

    RoleModel roleModel1 = getRoleModel1();

    dao.save(roleModel1);

    roleModel1.setName("RoleModel1.UPDATED");

    RoleModel found1 = dao.findById(roleModel1.getId()).get();
    assertEquals("RoleModel1", found1.getName());

    dao.update(roleModel1);

    RoleModel found2 = dao.findById(roleModel1.getId()).get();
    assertEquals("RoleModel1.UPDATED", found2.getName());

  }

  @Test
  void testGetIsAdmin() {
    RoleModel roleModel1 = getRoleModel1();
    RoleModel roleModel2 = getRoleModel2();
    RoleModel roleModel3 = getRoleModel3isAdminRoleName();

    dao.saveAll(Arrays.asList(roleModel1, roleModel2, roleModel3));

    RoleModel isAdminRole = dao.getIsAdmin();

    assertNotEquals(roleModel1, isAdminRole);
    assertNotEquals(roleModel2, isAdminRole);
    assertEquals(roleModel3, isAdminRole);
  }

  @Test
  void testFindAllByRoleType() {
    RoleModel roleModel1 = getRoleModel1();
    RoleModel roleModel2 = getRoleModel2();
    RoleModel roleModel3 = getRoleModel3isAdminRoleName();
    assertEquals(RoleType.VAULT, roleModel1.getType());
    assertEquals(RoleType.SCHOOL, roleModel2.getType());
    assertEquals(RoleType.VAULT, roleModel3.getType());

    dao.saveAll(Arrays.asList(roleModel1, roleModel2, roleModel3));

    Collection<RoleModel> all = dao.findAll((RoleType) null);
    assertEquals(0, all.size());

    Collection<RoleModel> admins = dao.findAll(RoleType.ADMIN);
    assertEquals(0, admins.size());

    Collection<RoleModel> schools = dao.findAll(RoleType.SCHOOL);
    assertEquals(1, schools.size());
    assertTrue(schools.contains(roleModel2));

    Collection<RoleModel> vaults = dao.findAll(RoleType.VAULT);
    assertEquals(2, vaults.size());
    assertTrue(vaults.contains(roleModel1));
    assertTrue(vaults.contains(roleModel3));
  }

  @Test
  void testFindAllEditableRoles() {
    assertTrue(dao.findAllEditableRoles().isEmpty());

    RoleModel roleModel1 = getRoleModel1();
    RoleModel roleModel2 = getRoleModel2();
    RoleModel roleModel3 = getRoleModel3isAdminRoleName();
    RoleModel roleModel4 = getRoleModel4adminRoleType();
    assertEquals(RoleType.VAULT, roleModel1.getType());
    assertEquals(RoleType.SCHOOL, roleModel2.getType());
    assertEquals(RoleType.VAULT, roleModel3.getType());
    assertEquals(RoleType.ADMIN, roleModel4.getType());

    dao.saveAll(Arrays.asList(roleModel1, roleModel2, roleModel3, roleModel4));
    List<RoleModel> editable = dao.findAllEditableRoles();
    assertEquals(3, editable.size());
    assertTrue(editable.contains(roleModel1));
    assertTrue(editable.contains(roleModel2));
    assertTrue(editable.contains(roleModel3));
    assertFalse(editable.contains(roleModel4));
  }

  @Test
  void testGetDataOwner() {
    assertNull(dao.getDataOwner());

    RoleModel roleModel1 = getRoleModel1();
    RoleModel roleModel2 = getRoleModel2();
    RoleModel roleModel3 = getRoleModel3isAdminRoleName();
    RoleModel roleModel4 = getRoleModel4adminRoleType();
    RoleModel roleModel5 = getRoleModel5dataOwner();
    dao.saveAll(Arrays.asList(roleModel1, roleModel2, roleModel3, roleModel4, roleModel5));

    RoleModel dataOwner = dao.getDataOwner();
    assertEquals(roleModel5, dataOwner);
  }

  @Test
  void testGetDepositor() {
    assertNull(dao.getDepositor());

    RoleModel roleModel1 = getRoleModel1();
    RoleModel roleModel2 = getRoleModel2();
    RoleModel roleModel3 = getRoleModel3isAdminRoleName();
    RoleModel roleModel4 = getRoleModel4adminRoleType();
    RoleModel roleModel5 = getRoleModel5dataOwner();
    RoleModel roleModel6 = getRoleModel6depositor();
    dao.saveAll(Arrays.asList(roleModel1, roleModel2, roleModel3, roleModel4, roleModel5, roleModel6));

    RoleModel depositor = dao.getDepositor();
    assertEquals(roleModel6, depositor);
  }


  @Test
  void testGetNominatedDataManager() {
    assertNull(dao.getNominatedDataManager());

    RoleModel roleModel1 = getRoleModel1();
    RoleModel roleModel2 = getRoleModel2();
    RoleModel roleModel3 = getRoleModel3isAdminRoleName();
    RoleModel roleModel4 = getRoleModel4adminRoleType();
    RoleModel roleModel5 = getRoleModel5dataOwner();
    RoleModel roleModel6 = getRoleModel6depositor();
    RoleModel roleModel7 = getRoleModel7nominatedDataManager();
    dao.saveAll(Arrays.asList(roleModel1, roleModel2, roleModel3,
        roleModel4, roleModel5, roleModel6, roleModel7));

    RoleModel nominatedDataManager = dao.getNominatedDataManager();
    assertEquals(roleModel7, nominatedDataManager);
  }

  @Test
  void testGetVaultCreator() {
    assertNull(dao.getVaultCreator());

    RoleModel roleModel1 = getRoleModel1();
    RoleModel roleModel2 = getRoleModel2();
    RoleModel roleModel3 = getRoleModel3isAdminRoleName();
    RoleModel roleModel4 = getRoleModel4adminRoleType();
    RoleModel roleModel5 = getRoleModel5dataOwner();
    RoleModel roleModel6 = getRoleModel6depositor();
    RoleModel roleModel7 = getRoleModel7nominatedDataManager();
    RoleModel roleModel8 = getRoleModel8nominatedDataManager();
    dao.saveAll(Arrays.asList(roleModel1, roleModel2, roleModel3,
        roleModel4, roleModel5, roleModel6, roleModel7, roleModel8));

    RoleModel vaultCreator = dao.getVaultCreator();
    assertEquals(roleModel8, vaultCreator);
  }

  @Nested
  class StoreSpecialRoles {

    @Test
    void testStoreSpecialRoles_AllRolesCreated() {

      assertEquals(0, dao.list().size());
      assertNull(dao.getIsAdmin());
      assertNull(dao.getDataOwner());
      assertNull(dao.getDepositor());
      assertNull(dao.getNominatedDataManager());
      assertNull(dao.getVaultCreator());
      dao.storeSpecialRoles();
      List<RoleModel> all = dao.list();
      assertEquals(5, all.size());
      RoleModel isAdmin = dao.getIsAdmin();
      RoleModel dataOwner = dao.getDataOwner();
      RoleModel depositor = dao.getDepositor();
      RoleModel nominatedDataManager = dao.getNominatedDataManager();
      RoleModel vaultCreator = dao.getVaultCreator();

      Set<Long> expectedIds = Stream.of(isAdmin, dataOwner,
              depositor, nominatedDataManager,
              vaultCreator)
          .map(RoleModel::getId)
          .collect(Collectors.toSet());

      Set<Long> actualIds = all.stream()
          .map(RoleModel::getId)
          .collect(Collectors.toSet());

      assertEquals(expectedIds, actualIds);
    }

    @Test
    void testStoreSpecialRoles_SomeRolesReCreated() {

      assertEquals(0, dao.list().size());
      assertNull(dao.getIsAdmin());
      assertNull(dao.getDataOwner());
      assertNull(dao.getDepositor());
      assertNull(dao.getNominatedDataManager());
      assertNull(dao.getVaultCreator());
      dao.storeSpecialRoles();
      List<RoleModel> all = dao.list();
      assertEquals(5, all.size());
      RoleModel isAdmin1 = dao.getIsAdmin();
      RoleModel dataOwner1 = dao.getDataOwner();
      RoleModel depositor1 = dao.getDepositor();
      RoleModel nominatedDataManager1 = dao.getNominatedDataManager();
      RoleModel vaultCreator1 = dao.getVaultCreator();

      Set<Long> expectedIds1 = Stream.of(
              isAdmin1, vaultCreator1,
              nominatedDataManager1,
              dataOwner1, depositor1)
          .map(RoleModel::getId)
          .collect(Collectors.toSet());

      Set<Long> actualIds1 = all.stream()
          .map(RoleModel::getId)
          .collect(Collectors.toSet());

      assertEquals(expectedIds1, actualIds1);

    /*
    Delete 2 roles are call 'storeSpecialRoles' again,
    check they are re-created with different ids
     */

      dao.delete(dataOwner1);
      dao.delete(depositor1);
      dao.storeSpecialRoles();

      RoleModel isAdmin2 = dao.getIsAdmin();
      RoleModel dataOwner2 = dao.getDataOwner();
      RoleModel depositor2 = dao.getDepositor();
      RoleModel nominatedDataManager2 = dao.getNominatedDataManager();
      RoleModel vaultCreator2 = dao.getVaultCreator();

      assertEquals(isAdmin1.getId(), isAdmin2.getId());
      assertEquals(nominatedDataManager1.getId(), nominatedDataManager2.getId());
      assertEquals(vaultCreator1.getId(), vaultCreator2.getId());
      assertNotEquals(dataOwner1.getId(), dataOwner2.getId());
      assertNotEquals(depositor1.getId(), depositor2.getId());

      Set<Long> expectedIds2 = Stream.of(
              isAdmin1,vaultCreator1,
              nominatedDataManager1,
              dataOwner2, depositor2)
          .map(RoleModel::getId)
          .collect(Collectors.toSet());

      Set<Long> actualIds2 = dao.list().stream()
          .map(RoleModel::getId)
          .collect(Collectors.toSet());

      assertEquals(expectedIds2, actualIds2);
    }

    /*
      Tests that role_permissions in will be updated if they are not correct.
      We have to run this test in a transaction otherwise we get dreaded LazyInitializationExceptions.
      This test helps increase Code Coverage
     */
    @Test
    @Transactional
    void testStoreSpecialRoles_SomeRolePermissionsUpdated(@Autowired EntityManager em){

      assertEquals(0, dao.list().size());
      assertNull(dao.getIsAdmin());
      assertNull(dao.getDataOwner());
      assertNull(dao.getDepositor());
      assertNull(dao.getNominatedDataManager());
      assertNull(dao.getVaultCreator());

      permissionDAO.synchronisePermissions();
      dao.storeSpecialRoles();
      em.flush();

      long roleCount = getCountOfRoles();
      assertEquals(5, roleCount);

      long permissionCount = getCountOfPermissions();
      assertEquals(31, permissionCount);

      long rolePermissionCount = getCountOfRolePermissions();
      assertEquals(43, rolePermissionCount);

      assertEquals(5, dao.list().size());
      assertNotNull(dao.getIsAdmin());
      assertNotNull(dao.getDataOwner());
      assertNotNull(dao.getDepositor());
      assertNotNull(dao.getNominatedDataManager());
      assertNotNull(dao.getVaultCreator());

      RoleModel isAdmin3 = dao.getIsAdmin();
      RoleModel dataOwner3 = dao.getDataOwner();
      RoleModel depositor3 = dao.getDepositor();
      RoleModel nominatedDataManager3 = dao.getNominatedDataManager();
      RoleModel vaultCreator3 = dao.getVaultCreator();

      //we don't have to worry about 'Depositor' and 'NominatedDataManager' - they have no permissions
      assertEquals(0, getCountOfPermissionsForRole(depositor3));
      assertEquals(0, getCountOfPermissionsForRole(nominatedDataManager3));

      //we are just concerned with 'isAmdin', 'DataOwner' and 'VaultCreator'
      assertNotEquals(0, getCountOfPermissionsForRole(isAdmin3));
      assertNotEquals(0, getCountOfPermissionsForRole(dataOwner3));
      assertNotEquals(0, getCountOfPermissionsForRole(vaultCreator3));

      isAdmin3.setPermissions(Collections.emptyList());
      dataOwner3.setPermissions(Collections.emptyList());
      vaultCreator3.setPermissions(Collections.emptyList());

      dao.update(isAdmin3);
      dao.update(dataOwner3);
      dao.update(vaultCreator3);
      em.flush();

      //Check that the changes have been flushed through to the database
      assertEquals(0, getCountOfPermissionsForRole(isAdmin3));
      assertEquals(0, getCountOfPermissionsForRole(dataOwner3));
      assertEquals(0, getCountOfPermissionsForRole(vaultCreator3));

      dao.storeSpecialRoles();
      em.flush();

      //Check that the changes have been flushed through to the database
      assertNotEquals(0, getCountOfPermissionsForRole(isAdmin3));
      assertNotEquals(0, getCountOfPermissionsForRole(dataOwner3));
      assertNotEquals(0, getCountOfPermissionsForRole(vaultCreator3));
    }
  }

  private Long getCountOfPermissionsForRole(RoleModel role){
    RowMapper<Long> longRowMapper = new SingleColumnRowMapper<>();
    return template.queryForObject("select count(*) from `Role_permissions` XYZ where XYZ.role_id=?",
        longRowMapper, role.getId());
  }

  private Long getCountOfRolePermissions(){
    return template.queryForObject("select count(*) from `Role_permissions`", Long.class);
  }

  private Long getCountOfPermissions() {
    return template.queryForObject("select count(*) from `Permissions`", Long.class);
  }

  private Long getCountOfRoles() {
    return template.queryForObject("select count(*) from `Roles`", Long.class);
  }

  @Test
  void testListAndPopulate() {

    dao.storeSpecialRoles();

    RoleModel isAdminRole1 = dao.getIsAdmin();
    assertEquals(0, isAdminRole1.getAssignedUserCount());

    User user1 = new User();
    user1.setID("user1");
    user1.setFirstname("first1");
    user1.setLastname("last1");
    user1.setEmail("first1.last1@test.com");

    User user2 = new User();
    user2.setID("user2");
    user2.setFirstname("first2");
    user2.setLastname("last2");
    user2.setEmail("first2.last2@test.com");

    userDAO.save(user1);
    userDAO.save(user2);

    RoleAssignment ra1 = new RoleAssignment();
    ra1.setUserId(user1.getID());
    ra1.setRole(isAdminRole1);
    roleAssignmentDAO.save(ra1);

    RoleAssignment ra2 = new RoleAssignment();
    ra2.setUserId(user2.getID());
    ra2.setRole(isAdminRole1);
    roleAssignmentDAO.save(ra2);

    assertEquals(0, isAdminRole1.getAssignedUserCount());

    //evict the RoleModel so we get it fresh from the db.
    Cache cache = em.getEntityManagerFactory().getCache();
    cache.evict(RoleModel.class, isAdminRole1.getId());

    Optional<RoleModel> optRoleWithUserCount = dao.listAndPopulate().stream().
        filter(r -> r.getAssignedUserCount() > 0).min(Comparator.comparing(RoleModel::getName));

    assertTrue(optRoleWithUserCount.isPresent());

    RoleModel roleModelWithAssignedUserCount = optRoleWithUserCount.get();
    assertEquals(isAdminRole1.getId(), roleModelWithAssignedUserCount.getId());
    assertEquals(2, roleModelWithAssignedUserCount.getAssignedUserCount());
  }

  @BeforeEach
  void setup() {
    template.execute("delete from `Role_permissions`");
    template.execute("delete from `Permissions`");
    template.execute("delete from `GroupOwners`");
    template.execute("delete from `Groups`");
    template.execute("delete from `Role_assignments`");
    template.execute("delete from `Users`");
    template.execute("delete from `Roles`");
    assertEquals(0, dao.count());
  }

  public static RoleModel getRoleModel1() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName("RoleModel1");
    roleModel.setStatus("STATUS1");
    roleModel.setType(RoleType.VAULT);
    return roleModel;
  }

  public static RoleModel getRoleModel2() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName("RoleModel2");
    roleModel.setStatus("STATUS2");
    roleModel.setType(RoleType.SCHOOL);
    return roleModel;
  }

  public static RoleModel getRoleModel3isAdminRoleName() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName(RoleUtils.IS_ADMIN_ROLE_NAME);
    roleModel.setStatus("STATUS3");
    roleModel.setType(RoleType.VAULT);
    return roleModel;
  }


  public static RoleModel getRoleModel4adminRoleType() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName("myAdminRole");
    roleModel.setStatus("STATUS4");
    roleModel.setType(RoleType.ADMIN);
    return roleModel;
  }

  public static RoleModel getRoleModel5dataOwner() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName(RoleUtils.DATA_OWNER_ROLE_NAME);
    roleModel.setStatus("STATUS5");
    roleModel.setType(RoleType.ADMIN);
    return roleModel;
  }

  public static RoleModel getRoleModel6depositor() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName(RoleUtils.DEPOSITOR_ROLE_NAME);
    roleModel.setStatus("STATUS6");
    roleModel.setType(RoleType.ADMIN);
    return roleModel;
  }

  public static RoleModel getRoleModel7nominatedDataManager() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName(RoleUtils.NOMINATED_DATA_MANAGER_ROLE_NAME);
    roleModel.setStatus("STATUS7");
    roleModel.setType(RoleType.ADMIN);
    return roleModel;
  }

  public static RoleModel getRoleModel8nominatedDataManager() {
    RoleModel roleModel = new RoleModel();
    roleModel.setName(RoleUtils.VAULT_CREATOR_ROLE_NAME);
    roleModel.setStatus("STATUS8");
    roleModel.setType(RoleType.ADMIN);
    return roleModel;
  }

}
