package org.datavaultplatform.broker.services;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class EmailServiceTest extends BaseEmailServiceTest {


  private static Map<String, Object> TEMPLATE_MODEL = new HashMap<String, Object>() {{
    put("audit-id", "aud-id-001");
    put("timestamp", "timestamp-002");
    put("chunk-id", "chunk-id-003");
    put("archive-id", "archive-id-004");
    put("location", "location-005");
  }};

  String EXPECTED_EMAIL_MSG_BODY = "<html>\n"
      + "\t<body>\n"
      + "\t\t\n"
      + "\t\t<p>The attempted audit of the following chunk failed:</p>\n"
      + "\t\t<ul>\n"
      + "\t\t\t<li>Audit ID: aud-id-001</li>\n"
      + "\t\t\t<li>Date of failed operation: timestamp-002</li>\n"
      + "\t\t\t<li>Chunk ID: chunk-id-003</li>\n"
      + "\t\t\t<li>Archive ID: archive-id-004</li>\n"
      + "\t\t\t<li>location: location-005</li>\n"
      + "\t\t</ul>\n"
      + "\t</body>\n"
      + "</html>";

  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) {
    setupMailProperties(registry);
  }

  @Test
  void testPlainTextEmail() {
    String rand1 = UUID.randomUUID().toString();
    String rand2 = UUID.randomUUID().toString();
    String to = "james.bond@example.com";
    String subject = "sub-" + rand1;
    String message = "msg-" + rand2;

    emailService.sendPlaintextMail(to, subject, message);
    ResponseEntity<String> events = lookupSentEmailsContaining(rand1);
    checkSentEmail(events, subject, EXPECTED_FROM, to, message);
    verifyNoInteractions(mUserService);
  }

  @Test
  void testSendTemplateMail() {
    String to = "james.bond.1@example.com";
    String subject = "sub-" + UUID.randomUUID();
    emailService.sendTemplateMail(to, subject, EmailTemplate.AUDIT_CHUNK_ERROR, TEMPLATE_MODEL);
    ResponseEntity<String> events = lookupSentEmailsContaining(subject);
    checkSentEmail(events, subject, EXPECTED_FROM, to, EXPECTED_EMAIL_MSG_BODY);
    verifyNoInteractions(mUserService);
  }

  @Test
  void testSendTemplateMailToUser() {
    String userid = "user-006";
    String email = "user.006@example.com";
    User user = new User();
    user.setID(userid);
    user.setEmail(email);

    String subject = "sub-" + UUID.randomUUID();
    emailService.sendTemplateMailToUser(user, subject, EmailTemplate.AUDIT_CHUNK_ERROR,
        TEMPLATE_MODEL);

    ResponseEntity<String> events = lookupSentEmailsContaining(subject);
    checkSentEmail(events, subject, EXPECTED_FROM, email, EXPECTED_EMAIL_MSG_BODY);
    verifyNoInteractions(mUserService);
  }

  @Test
  void testSendTemplateMailToUserId() {
    String userid = "user-007";
    String email = "user.007@example.com";
    User user = new User();
    user.setID(userid);
    user.setEmail(email);

    when(mUserService.getUser(userid)).thenReturn(user);

    String subject = "sub-" + UUID.randomUUID();
    emailService.sendTemplateMailToUser(userid, subject, EmailTemplate.AUDIT_CHUNK_ERROR,
        TEMPLATE_MODEL);

    ResponseEntity<String> events = lookupSentEmailsContaining(subject);
    checkSentEmail(events, subject, EXPECTED_FROM, email, EXPECTED_EMAIL_MSG_BODY);

    verify(mUserService).getUser(userid);
    verifyNoMoreInteractions(mUserService);
  }


}
