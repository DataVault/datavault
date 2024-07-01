package org.datavaultplatform.common.model.dao;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.DepositPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
public class DepositPathDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  DepositPathDAO dao;

  @Test
  void testWriteThenRead() {
    DepositPath path1 = new DepositPath();
    path1.setFilePath("file-path-1");

    dao.save(path1);
    assertThat(path1).isNotNull();
    UUIDUtils.assertIsUUID(path1.getID());
  }


}
