package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 Uses docker image mailhog/mailhob to test emails have been sent correctly.
 See https://github.com/mailhog/MailHog
 See https://hub.docker.com/r/mailhog/mailhog
 */
@SpringBootTest(classes = DataVaultBrokerApp.class)
@Testcontainers
@TestPropertySource(properties = {
    "broker.controllers.enabled=false",
    "broker.services.enabled=false",
    "broker.database.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.initialise.enabled=false",
})
@AddTestProperties
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.hbm2ddl.auto=none",
    "mail.administrator=test@datavaultplatform.org"})
@Import(EmailService.class)
@Slf4j
public class EmailServiceTest {

  public static final String EXPECTED_FROM = "test@datavaultplatform.org";

  @MockBean
  DataSource datasource;

  public static final String MAILHOG_IMAGE_NAME = "mailhog/mailhog:v1.0.1";

  public static final int PORT_SMTP = 1025;
  public static final int PORT_HTTP = 8025;

  @Value("${tc.mailhog.http}")
  int mailhogHttpPort;

  @Autowired
  JavaMailSender sender;

  @Value("${mail.host}")
  String host;

  @MockBean
  UsersService mUserService;

  @MockBean
  ClientsService clientsService;

  @MockBean
  AdminService adminService;

  @MockBean
  RolesAndPermissionsService rolesAndPermissionsService;

  @Autowired
  EmailService emailService;

  private static Map<String,Object> TEMPLATE_MODEL = new HashMap<String,Object>(){{
    put("audit-id","aud-id-001");
    put("timestamp","timestamp-002");
    put("chunk-id","chunk-id-003");
    put("archive-id","archive-id-004");
    put("location","location-005");
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

  @Container
  private static GenericContainer<?> topLevelContainer = new GenericContainer<>(MAILHOG_IMAGE_NAME)
      .withExposedPorts(PORT_SMTP, PORT_HTTP);

  @DynamicPropertySource
  static void postgresqlProperties(DynamicPropertyRegistry registry) {
    registry.add("tc.mailhog.http", () -> topLevelContainer.getMappedPort(PORT_HTTP));
    registry.add("mail.host", topLevelContainer::getHost);
    registry.add("mail.port", () -> topLevelContainer.getMappedPort(PORT_SMTP));
  }

  @Test
  void testPlainTextEmail() {
    String rand1 = UUID.randomUUID().toString();
    String rand2 = UUID.randomUUID().toString();
    String to = "james.bond@example.com";
    String subject = "sub-"+rand1;
    String message = "msg-"+rand2;

    emailService.sendPlaintextMail(to, subject, message);
    ResponseEntity<String> events = lookupSentEmailsContaining(rand1);
    checkSentEmail(events, subject, EXPECTED_FROM, to, message);
    verifyNoInteractions(mUserService);
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

  @Test
  void testSendTemplateMailToUser() {
    String userid = "user-006";
    String email = "user.006@example.com";
    User user = new User();
    user.setID(userid);
    user.setEmail(email);

    String subject = "sub-"+ UUID.randomUUID();
    emailService.sendTemplateMailToUser(user, subject, EmailTemplate.AUDIT_CHUNK_ERROR, TEMPLATE_MODEL);

    ResponseEntity<String> events = lookupSentEmailsContaining(subject);
    checkSentEmail(events, subject, EXPECTED_FROM, email, EXPECTED_EMAIL_MSG_BODY);
    verifyNoInteractions(mUserService);
  }

  @Test
  void testSendTemplateMailToUserId(){
    String userid = "user-007";
    String email = "user.007@example.com";
    User user = new User();
    user.setID(userid);
    user.setEmail(email);

    when(mUserService.getUser(userid)).thenReturn(user);

    String subject = "sub-"+ UUID.randomUUID();
    emailService.sendTemplateMailToUser(userid, subject, EmailTemplate.AUDIT_CHUNK_ERROR, TEMPLATE_MODEL);

    ResponseEntity<String> events = lookupSentEmailsContaining(subject);
    checkSentEmail(events, subject, EXPECTED_FROM, email, EXPECTED_EMAIL_MSG_BODY);

    verify(mUserService).getUser(userid);
    verifyNoMoreInteractions(mUserService);
  }

  private ResponseEntity<String> lookupSentEmailsContaining(String query) {
    RestTemplate rt = new RestTemplate();
    ResponseEntity<String> events = rt.getForEntity(
        String.format("http://%s:%d/api/v2/search?kind=containing&query=" + query, this.host,
            this.mailhogHttpPort),
        String.class);
    log.info("events {}",events);
    return events;
  }

  void checkSentEmail(ResponseEntity<String> events, String expectedSubject, String expectedFrom, String expectedTo, String expectedMessage){
    assertEquals(HttpStatus.OK, events.getStatusCode());

    DocumentContext ctx = JsonPath.parse(events.getBody());

    int total = ctx.read("$.total", Integer.class);
    assertEquals(1, total);

    int count = ctx.read("$.count", Integer.class);
    assertEquals(1, count);

    String actualSubject = ctx.read("$.items[0].Content.Headers.Subject[0]");
    String actualFrom = ctx.read("$.items[0].Content.Headers.From[0]");
    String actualTo = ctx.read("$.items[0].Content.Headers.To[0]");
    String actualMessage = ctx.read("$.items[0].Content.Body");

    assertEquals(expectedFrom, actualFrom);
    assertEquals(expectedMessage, TestUtils.useNewLines(actualMessage));
    assertEquals(expectedSubject, actualSubject);
    assertEquals(expectedTo, actualTo);
  }

}
