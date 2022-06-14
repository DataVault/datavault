package org.datavaultplatform.worker.app;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
//import com.rabbitmq.client.impl.CredentialsRefreshService;

@SpringBootTest
@Slf4j
@AddTestProperties
public class DataVaultWorkerInstanceAppTest {

  @Test
  void testContextLoads() {
    log.info("ta-da!");
  }
}
