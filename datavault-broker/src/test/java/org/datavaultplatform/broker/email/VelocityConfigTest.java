package org.datavaultplatform.broker.email;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.VelocityEngine;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.email.EmailTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@Slf4j
@DirtiesContext
@EnableAutoConfiguration(exclude= {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class })
@TestPropertySource(properties = {
    "broker.controllers.enabled=false",
    "broker.services.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.initialise.enabled=false",
    "broker.database.enabled=false"})
@AddTestProperties
@Import(MockServicesConfig.class) //spring security relies on services
public class VelocityConfigTest {

  @Autowired
  VelocityEngine engine;

  @Autowired
  TemplateResolver resolver;

  @ParameterizedTest
  @MethodSource("templateNameProvider")
  void testTemplatesExist(String templateName){
    assertTrue(doesTemplateExist(templateName));
  }

  @Test
  void testBadTemplate() {
    assertFalse(doesTemplateExist("nonExistantTemplate.vm"));
  }

  private boolean doesTemplateExist(String templateName) {
    return engine.resourceExists(resolver.resolve(templateName));
  }

  private static Stream<Arguments> templateNameProvider() {
      return Arrays.stream(EmailTemplate.class.getDeclaredFields())
          .filter(VelocityConfigTest::isPublicStaticFinalString)
          .map(VelocityConfigTest::getTemplateName)
          .map(Arguments::of);
  }

  @SneakyThrows
  private static String getTemplateName(Field templateNameField){
    String templateName =  (String)templateNameField.get(null);
    checkEmailTemplateConstantField(templateNameField, templateName);
    return templateName;
  }

  private static void checkEmailTemplateConstantField(Field templateNameField, String templateName){
    //double check that the name of the 'public static final String' template constant maps to the name of the template file
    String expectedTemplateName = templateNameField.getName()
        .toLowerCase()
        .replace("_", "-") + ".vm";
    assertEquals(expectedTemplateName, templateName);
  }

  private static boolean isPublicStaticFinalString(Field f) {
    int mods = f.getModifiers();
    return isPublic(mods) && isStatic(mods) &&
           isFinal(mods)  && f.getType().equals(String.class);
  }
}
