package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.RoleModel;
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
    "broker.initialise.enabled=true",
    "broker.scheduled.enabled=false"
})
public class RoleDAO2IT extends BaseDatabaseTest {

  @Autowired
  RoleDAO dao;

  @Autowired
  ObjectMapper mapper;

  @Test
  void testEntityGraph() {
    assertEquals(7, dao.count());

    RoleModel dataOwner = dao.getDataOwner();
    writeToString(dataOwner);

    RoleModel depositor = dao.getDepositor();
    writeToString(depositor);

    RoleModel isAdmin = dao.getIsAdmin();
    assertNotNull(isAdmin.getPermissions().iterator().next().getPermission());
    writeToString(isAdmin);

    RoleModel nominatedDataManager = dao.getNominatedDataManager();
    writeToString(nominatedDataManager);

    RoleModel vaultCreator = dao.getVaultCreator();
    writeToString(vaultCreator);

    List<RoleModel> list = dao.list();
    writeToString(list);

    List<RoleModel> all = dao.findAll();
    writeToString(all);

    RoleModel found = dao.findById(isAdmin.getId()).get();
    writeToString(found);

    List<RoleModel> editable = dao.findAllEditableRoles();
    writeToString(editable);

    List<RoleModel> populated = dao.listAndPopulate();
    writeToString(populated);
  }

  @SneakyThrows
  void writeToString(Object obj) {
      String result = mapper.writeValueAsString(obj);
      log.info(result);
  }

}
