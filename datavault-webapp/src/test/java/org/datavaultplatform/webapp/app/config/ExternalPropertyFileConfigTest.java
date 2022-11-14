package org.datavaultplatform.webapp.app.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.config.BaseExternalPropertyFileConfigTest;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

/**
 * The Broker AND WebApp will read from external properties files.
 * The location of the external properties files is based on environmental variables:
 * HOME, DATAVAULT_HOME and DATAVAULT_ETC
 * This test:
 *   creates some temp directories,
 *   create properties files in the temp directories
 *   sets ENV variables(HOME, DEFAULT_HOME and DATAVAULT_ETC) to the temp directories,
 *   checks that the application can read the properties via PropertiesConfig using ENV variables
 */
@SpringBootTest
@ProfileStandalone
@Slf4j
@DirtiesContext
public class ExternalPropertyFileConfigTest extends BaseExternalPropertyFileConfigTest {
}
