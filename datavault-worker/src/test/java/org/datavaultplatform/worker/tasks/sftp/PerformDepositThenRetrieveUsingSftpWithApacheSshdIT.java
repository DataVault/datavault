package org.datavaultplatform.worker.tasks.sftp;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@TestPropertySource(properties = {"sftp.driver.use.apache.sshd=false"})
@Slf4j
public class PerformDepositThenRetrieveUsingSftpWithApacheSshdIT extends BasePerformDepositThenRetrieveUsingSftpIT {
}