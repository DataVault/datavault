package org.datavaultplatform.broker.test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.initialise.InitialiseDatabase;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.GroupDAO;
import org.datavaultplatform.common.model.dao.PermissionDAO;
import org.datavaultplatform.common.model.dao.RoleAssignmentDAO;
import org.datavaultplatform.common.model.dao.RoleDAO;
import org.datavaultplatform.common.model.dao.UserDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.MySQLContainer;

@Slf4j
@DirtiesContext
@EnabledIf("#{T(org.testcontainers.DockerClientFactory).instance().isDockerAvailable()}")
public abstract class BaseReuseDatabaseTest  {

  // This container is once per class - not once per method. Methods can 'dirty' the database.
  static MySQLContainer<?> mysql = new MySQLContainer<>(BaseDatabaseTest.MYSQL_IMAGE_NAME).withReuse(true);
  @Autowired
  InitialiseDatabase initialiseDatabase;

  @Autowired
  protected JdbcTemplate template;

  @BeforeAll
  public static void beforeAll() {
    mysql.start();
  }

  @Autowired
  UserDAO userDAO;

  @Autowired
  RoleDAO roleDAO;

  @Autowired
  RoleAssignmentDAO roleAssignmentDAO;

  @Autowired
  PermissionDAO permissionDAO;

  @BeforeEach
  void cleanDB() {

    boolean allEmpty;

    List<String> tableNames = getTableNames();

    int i = 0;
    do {
      i++;
      tableNames.forEach(this::emptyTable);
      allEmpty = allEmpty(tableNames);
    } while (i < tableNames.size() && !allEmpty);
    log.info("after [{}] iterations, allEmpty={}", i, allEmpty);

    initialiseDatabase.initialiseDatabase();

    boolean postInitAllEmpty = allEmpty(tableNames);

    assertFalse(postInitAllEmpty);

  }

  List<String> getTableNames() {
    String SQL_2 = "show tables";
    List<String> tableNames = template.query(SQL_2,
        (rs, rowNum) -> rs.getString(1));
    return tableNames.stream().filter(name ->  !name.equalsIgnoreCase("hibernate_sequence")).collect(Collectors.toList());
  }

  void emptyTable(String tableName) {
    try {
      template.execute(String.format("delete from %s", tableName));
    } catch (DataAccessException ex) {
      // errors are expected when we try and delete tables without regard to Foreign Key Constaints
      log.debug("oops " + ex.getMessage());
    }
  }

  public boolean allEmpty(List<String> tableNames) {
    for (String tableName : tableNames) {
      long count = template.queryForObject("select count(*) from " + tableName, Long.class);
      if (count > 0) {
        return false;
      }
    }
    return true;
  }

  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
  }

  protected User createTestUser(String userId, String schoolId, Permission... permissions) {
    return BaseDatabaseTest.createUserWithPermissions(userDAO, permissionDAO, roleDAO, roleAssignmentDAO,  userId, schoolId, permissions);
  }
}
