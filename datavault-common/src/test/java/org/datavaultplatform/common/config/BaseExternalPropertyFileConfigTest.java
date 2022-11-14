package org.datavaultplatform.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.FileSystemUtils;

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
@Slf4j
@DirtiesContext
public abstract class BaseExternalPropertyFileConfigTest {

  private static final String RAND_1 = UUID.randomUUID().toString();
  private static final String RAND_2 = UUID.randomUUID().toString();
  private static final String RAND_3 = UUID.randomUUID().toString();

  static Path parentDir;
  static Path homeDir;
  static Path etcDir;
  static Path dvHomeDir;

  @Value("${prop.dv.home}")
  String fromDvHome;

  @Value("${prop.dv.etc}")
  String fromDvEtc;
  @Value("${prop.home}")
  String fromHome;

  @Value("${prop.override.test}")
  String overrideTest;

  @BeforeAll
  static void setupFiles() throws IOException {
    parentDir = Files.createTempDirectory("test").normalize();
    log.info("TEMP PROPERTY FILES PARENT DIR {}", parentDir);

    dvHomeDir = Files.createDirectories(parentDir.resolve("dvhome"));
    etcDir = Files.createDirectories(parentDir.resolve("etc"));
    homeDir = Files.createDirectories(parentDir.resolve("home"));


    createFile(dvHomeDir, "config/datavault.properties", "prop.dv.home="+RAND_1, "prop.override.test=1");
    createFile(etcDir, "datavault/datavault.properties", "prop.dv.etc="+RAND_2, "prop.override.test=2");
    createFile(homeDir, ".config/datavault/datavault.properties", "prop.home="+RAND_3, "prop.override.test=3");
  }

  static void createFile(Path parentDir, String relativePath, String... lines) throws IOException {
    Path filePath = parentDir.resolve(relativePath);
    Files.createDirectories(filePath.getParent());
    Files.write(filePath, Arrays.asList(lines), StandardCharsets.UTF_8);
  }

  /**
   * Tell Spring about new values of the ENV variables
   */
  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) {
    registry.add("DATAVAULT_HOME", () -> dvHomeDir.toString());
    registry.add("DATAVAULT_ETC", () -> etcDir.toString());
    registry.add("HOME", () -> homeDir.toString());
  }

  /**
   * This is just to be extra sure
   */
  @BeforeEach
  void preCheck(ApplicationContext ctx) {
    assertTrue(Files.isDirectory(dvHomeDir));
    assertTrue(Files.isDirectory(etcDir));
    assertTrue(Files.isDirectory(homeDir));

    Environment env = ctx.getEnvironment();
    assertEquals(env.getProperty("DATAVAULT_HOME"), dvHomeDir.toString());
    assertEquals(env.getProperty("DATAVAULT_ETC"), etcDir.toString());
    assertEquals(env.getProperty("HOME"), homeDir.toString());
  }

  /**
   * Tests that PropertiesConfig can read external properties files using ENV variables.
   */
  @Test
  void testPropertiesAreReadFromExternalPropertiesFiles() {
    assertEquals(RAND_1, fromDvHome);
    assertEquals(RAND_2, fromDvEtc);
    assertEquals(RAND_3, fromHome);
  }

  /**
   * When a property exists in all 3 of the external properties files,
   * values from PropertySources that were registered later take precedence.
   * In this case 'file://${HOME}/.config/datavault/datavault.properties'
   */
  @Test
  void testLaterRegisteredPropertySourceHasPriority() {
    assertEquals("3", overrideTest);
  }

  /**
   * Delete the temp properties files
   */
  @AfterAll
  static void tearDown() {
    boolean deleted = FileSystemUtils.deleteRecursively(parentDir.toFile());
    assertTrue(deleted);
  }
}
