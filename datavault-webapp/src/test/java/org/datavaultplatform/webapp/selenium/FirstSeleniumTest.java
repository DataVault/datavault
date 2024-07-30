package org.datavaultplatform.webapp.selenium;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RequiresUsernamePasswordEnvVariables
public class FirstSeleniumTest extends BaseSeleniumTest {

    @Test
    void testLoginGoesToVaultsPage() {
        checkHeader();
        checkFooter();
        assertThat(driver.getCurrentUrl()).isEqualTo(DV_DEMO_URL + "/vaults");
    }


}
