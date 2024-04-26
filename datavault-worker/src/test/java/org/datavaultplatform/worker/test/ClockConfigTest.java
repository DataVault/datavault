package org.datavaultplatform.worker.test;

import org.assertj.core.api.Assertions;
import org.datavaultplatform.common.storage.impl.SftpUtils;
import org.junit.jupiter.api.Test;

import java.time.Clock;

public class ClockConfigTest {
    
    @Test
    void testSftpDirectoryName() {
        Clock fixedClock = TestClockConfig.TEST_CLOCK;
        Assertions.assertThat(SftpUtils.getTimestampedDirectoryName(fixedClock))
                .isEqualTo("dv_20220329141516");
    }
}
