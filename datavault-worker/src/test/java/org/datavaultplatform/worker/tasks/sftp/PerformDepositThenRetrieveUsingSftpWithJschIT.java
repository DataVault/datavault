package org.datavaultplatform.worker.tasks.sftp;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@EnabledOnOs(OS.MAC)
@AddTestProperties
@DirtiesContext
@TestPropertySource(properties = {"sftp.driver.use.apache.sshd=true"})
@Slf4j
public class PerformDepositThenRetrieveUsingSftpWithJschIT extends BasePerformDepositThenRetrieveUsingSftpIT {
}