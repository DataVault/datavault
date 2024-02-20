package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.util.DaoUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
public class DaoUtilsIT extends BaseReuseDatabaseTest {

  @PersistenceContext
  EntityManager em;

  @Autowired
  PermissionDAO permissionDAO;

  @Autowired
  RoleDAO roleDAO;

  @Autowired
  RoleAssignmentDAO raDAO;

  @Autowired
  UserDAO userDAO;

  User user1;
  User user2;

  User user3;

  @Test
  @Transactional
  void testIsAdminRoleCoversAllPermissions() {
    assertEquals(0, userDAO.count());
    permissionDAO.synchronisePermissions();
    roleDAO.storeSpecialRoles();

    String jql = "select p.permission from org.datavaultplatform.common.model.RoleModel rm "
        + "INNER JOIN rm.permissions as p  where rm.name = 'IS Admin' and rm.type = :roleType";
    Query query = em.createQuery(jql);
    query.setParameter("roleType", RoleType.ADMIN);

    Set<Permission> permissionsForISadmin = new HashSet<>(
        (List<Permission>) query.getResultList());

    Set<Permission> allPermissions = Arrays.stream(Permission.values())
        .collect(Collectors.toSet());

    assertEquals(allPermissions, permissionsForISadmin);
  }

  private User getUser1() {
    User user = new User();
    user.setID("user1");
    user.setFirstname("firstname1");
    user.setLastname("lastname1");
    user.setEmail("user1@test.com");
    return user;
  }

  private User getUser2() {
    User user = new User();
    user.setID("user2");
    user.setFirstname("firstname2");
    user.setLastname("lastname2");
    user.setEmail("user2@test.com");
    return user;
  }

  private User getUser3() {
    User user = new User();
    user.setID("user3");
    user.setFirstname("firstname3");
    user.setLastname("lastname3");
    user.setEmail("user3@test.com");
    return user;
  }

  @SafeVarargs
  final <T> Set<T> setOf(T... items) {
    return new HashSet<>(Arrays.asList(items));
  }

  void setupUsers() {
    user1 = getUser1();
    userDAO.save(user1);

    user2 = getUser2();
    userDAO.save(user2);

    user3 = getUser3();
    userDAO.save(user3);
  }

  void setupAdminRoleAssignments() {
    RoleModel isAdmin = roleDAO.getIsAdmin();

    RoleAssignment ra1 = new RoleAssignment();
    ra1.setRole(isAdmin);
    ra1.setUserId(user1.getID());
    ra1.setSchoolId("SCHOOL1");

    RoleAssignment ra2 = new RoleAssignment();
    ra2.setRole(isAdmin);
    ra2.setUserId(user2.getID());
    ra2.setSchoolId("SCHOOL2");

    RoleAssignment ra3 = new RoleAssignment();
    ra3.setRole(isAdmin);
    ra3.setUserId(user2.getID());
    ra3.setSchoolId("SCHOOL3");

    RoleAssignment ra4 = new RoleAssignment();
    ra4.setRole(isAdmin);
    ra4.setUserId(user3.getID());
    ra4.setSchoolId(null);

    raDAO.save(ra1);
    raDAO.save(ra2);
    raDAO.save(ra3);
    raDAO.save(ra4);
  }

  void setupNonAdminRoleAssignments() {
    RoleModel nonAdmin = new RoleModel();
    nonAdmin.setName("nonAdmin");

    PermissionModel pm1 = permissionDAO.find(Permission.CAN_VIEW_QUEUES);
    PermissionModel pm2 = permissionDAO.find(Permission.CAN_VIEW_EVENTS);

    nonAdmin.setPermissions(Arrays.asList(pm1, pm2));
    nonAdmin.setStatus("active");
    nonAdmin.setDescription("non-admin-role");
    nonAdmin.setType(RoleType.VAULT);

    roleDAO.save(nonAdmin);

    RoleAssignment ra1 = new RoleAssignment();
    ra1.setRole(nonAdmin);
    ra1.setUserId(user1.getID());
    ra1.setSchoolId("SCHOOL1");

    RoleAssignment ra2 = new RoleAssignment();
    ra2.setRole(nonAdmin);
    ra2.setUserId(user2.getID());
    ra2.setSchoolId("SCHOOL2");

    RoleAssignment ra3 = new RoleAssignment();
    ra3.setRole(nonAdmin);
    ra3.setUserId(user2.getID());
    ra3.setSchoolId("SCHOOL3");

    raDAO.save(ra1);
    raDAO.save(ra2);
    raDAO.save(ra3);
  }

  @Nested
  class WithEntityManager {

    @Test
    void testGetPermittedSchoolIdsLinkedToAdminRole() {

      setupUsers();
      setupAdminRoleAssignments();

      //for each permission, there is one school for user1
      for (Permission p : Permission.values()) {
        Set<String> res1 = DaoUtils.getPermittedSchoolIds(em, user1.getID(), p);
        assertEquals(setOf("SCHOOL1"), res1);
      }

      //for each permission, there are two schools for user2
      for (Permission p : Permission.values()) {
        Set<String> res1 = DaoUtils.getPermittedSchoolIds(em, user2.getID(), p);
        assertEquals(setOf("SCHOOL2", "SCHOOL3"), res1);
      }

      //for each permission, there are no schools for user id 'XXX'
      for (Permission p : Permission.values()) {
        Set<String> res1 = DaoUtils.getPermittedSchoolIds(em, "XXX", p);
        assertEquals(Collections.emptySet(), res1);
      }

      //for each permission, there is the special case '*' for user3
      for (Permission p : Permission.values()) {
        Set<String> res1 = DaoUtils.getPermittedSchoolIds(em, user3.getID(), p);
        assertEquals(setOf("*"), res1);
      }
    }

    @Test
    void testGetPermittedSchoolIdsLinkedToNonAdminRole() {

      setupUsers();
      setupNonAdminRoleAssignments();

      List<Permission> validPerms = Arrays.asList(
          Permission.CAN_VIEW_QUEUES,
          Permission.CAN_VIEW_EVENTS);

      //for CAN_VIEW_QUEUES and CAN_VIEW_EVENTS, there is one school for user1
      for (Permission p : Permission.values()) {
        Set<String> res1 = DaoUtils.getPermittedSchoolIds(em, user1.getID(), p);
        if (validPerms.contains(p)) {
          assertEquals(setOf("SCHOOL1"), res1);
        } else {
          assertEquals(Collections.emptySet(), res1);
        }
      }

      //for CAN_VIEW_QUEUES and CAN_VIEW_EVENTS, there are two schools for user2
      for (Permission p : Permission.values()) {
        Set<String> res1 = DaoUtils.getPermittedSchoolIds(em, user2.getID(), p);
        if (validPerms.contains(p)) {
          assertEquals(setOf("SCHOOL2", "SCHOOL3"), res1);
        } else {
          assertEquals(Collections.emptySet(), res1);
        }
      }

      //for each permission, there are no schools for user id 'XXX'
      for (Permission p : Permission.values()) {
        Set<String> res1 = DaoUtils.getPermittedSchoolIds(em, "XXX", p);
        assertEquals(Collections.emptySet(), res1);
      }
    }
  }
}
