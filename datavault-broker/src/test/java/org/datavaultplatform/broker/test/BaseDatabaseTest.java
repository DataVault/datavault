package org.datavaultplatform.broker.test;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.PermissionDAO;
import org.datavaultplatform.common.model.dao.RoleAssignmentDAO;
import org.datavaultplatform.common.model.dao.RoleDAO;
import org.datavaultplatform.common.model.dao.UserDAO;
import org.datavaultplatform.common.util.UsesTestContainers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;

@UsesTestContainers
public abstract class BaseDatabaseTest {
  
  @Autowired
  UserDAO userDAO;

  @Autowired
  RoleDAO roleDAO;

  @Autowired
  RoleAssignmentDAO roleAssignmentDAO;

  @Autowired
  PermissionDAO permissionDAO;

  @PersistenceContext
  protected EntityManager em;
  
  @Autowired
  ApplicationContext ctx;
  
  @Container
  @ServiceConnection
  // This container is once per class - not once per method. Methods can 'dirty' the database.
  static final MariaDBContainer<?> mariadb = new MariaDBContainer<>(DockerImage.MARIADB_IMAGE);

  protected User createTestUser(String userId, String schoolId, Permission... permissions){
    return createUserWithPermissions(userDAO, permissionDAO, roleDAO, roleAssignmentDAO,  userId, schoolId, permissions);
  }

  static User createUserWithPermissions(
      UserDAO userDAO,
      PermissionDAO permissionDAO,
      RoleDAO roleDAO,
      RoleAssignmentDAO roleAssignmentDAO,
      String userId,
      String schoolId,
      Permission... permissions) {

    //create test user with specified 'usedId'
    User user = new User();
    user.setID(userId);
    user.setFirstname("first-"+userId);
    user.setLastname("last-"+userId);
    user.setEmail("test.user@test.com");
    userDAO.save(user);

    if(permissions.length > 0) {

      // create permissions
      List<PermissionModel> pms = Arrays.stream(permissions).map(p -> {
        PermissionModel pm = new PermissionModel();
        pm.setId(p.name());
        pm.setPermission(p);
        pm.setType(p.getDefaultType());
        pm.setLabel(p.getRoleName());
        permissionDAO.save(pm);
        return pm;
      }).toList();

      // create role with associated permissions
      RoleModel role = new RoleModel();
      role.setPermissions(pms);
      role.setName("test-role");
      role.setStatus("test-status"); //does this matter ?
      role.setType(RoleType.VAULT);//does this matter ?
      roleDAO.save(role);

      // link user with role
      RoleAssignment roleAssignment = new RoleAssignment();
      roleAssignment.setUserId(userId);
      roleAssignment.setRole(role);
      roleAssignment.setSchoolId(schoolId);
      roleAssignmentDAO.save(roleAssignment);
    }
    return user;
  }
}
