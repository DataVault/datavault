package org.datavaultplatform.broker.services;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.datavaultplatform.broker.config.EmailConfig;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.broker.services.BaseEmailServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Slf4j
public class ExternalVelocityTemplateEmailServiceIT extends BaseEmailServiceTest {

  @Autowired
  VelocityEngine engine;

  private static final Map<String,Object> TEMPLATE_MODEL = new HashMap<String,Object>(){{
    put("audit-id","ext-aud-id-001");
    put("timestamp","ext-timestamp-002");
    put("chunk-id","ext-chunk-id-003");
    put("archive-id","ext-archive-id-004");
    put("location","ext-location-005");
  }};

  public static final String EXTERNAL_TEMPLATE_CONTENTS = "<html><body><p>EXTERNAL TEST TEMPLATE</p>"
      + "<ul>"
      + "<li>Audit ID: ${audit-id}</li>"
      + "<li>Date of failed operation: ${timestamp}</li>"
      + "<li>Chunk ID: ${chunk-id}</li>"
      + "<li>Archive ID: ${archive-id}</li>"
      + "<li>location: ${location}</li>"
      + "</ul>"
      + "</body></html>";
  String EXPECTED_EMAIL_MSG_BODY = "<html><body><p>EXTERNAL TEST TEMPLATE</p><ul><li>Audit ID: ext-aud-id-001</li><li>Date of failed operation: ext-timestamp-002</li><li>Chunk ID: ext-chunk-id-003</li><li>Archive ID: ext-archive-id-004</li><li>location: ext-location-005</li></ul></body></html>";


  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) throws IOException {

    setupMailProperties(registry);

    //create temp directory
    File tmpDir = Files.createTempDirectory("testTemplate").toFile();
    //create 'mail-templates' directory beneath temp directory
    File tmpMailTemplateDir = Files.createDirectory(
        tmpDir.toPath().resolve(EmailConfig.DIR_MAIL_TEMPLATES)
    ).toFile();
    //create file for 'audit-chunk-error.vm' in <temp>/mail-templates
    File externalTemplate = new File(tmpMailTemplateDir, EmailTemplate.AUDIT_CHUNK_ERROR);

    //write test template contents for 'audit-chunk-error.vm' to 'external directory'
    FileUtils.writeStringToFile(externalTemplate, EXTERNAL_TEMPLATE_CONTENTS, UTF_8);

    String externalMailTemplateDir = tmpDir.getAbsolutePath();

    //Tell spring the value of 'external.mail.template.dir'
    registry.add(EmailConfig.EXTERNAL_EMAIL_TEMPLATE_DIR, () -> externalMailTemplateDir);
    log.info("{}=[{}]", EmailConfig.EXTERNAL_EMAIL_TEMPLATE_DIR, externalMailTemplateDir);
  }

  @Test
  void testExternalTemplateHasPreference(){
    Template temp = engine.getTemplate(
        Paths.get(EmailConfig.DIR_MAIL_TEMPLATES,
            EmailTemplate.AUDIT_CHUNK_ERROR).toString());
    assertNotNull(temp);
    assertEquals(FileResourceLoader.class.getName(), temp.getResourceLoader().getClassName());
  }

  @Test
  void testSendTemplateMail() {
    String to = "james.bond.1@example.com";
    String subject = "sub-"+ UUID.randomUUID();
    emailService.sendTemplateMail(to, subject, EmailTemplate.AUDIT_CHUNK_ERROR, TEMPLATE_MODEL);
    ResponseEntity<String> events = lookupSentEmailsContaining(subject);
    checkSentEmail(events, subject, EXPECTED_FROM, to, EXPECTED_EMAIL_MSG_BODY);
    verifyNoInteractions(mUserService);
  }
}
