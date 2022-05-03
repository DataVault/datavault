package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
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
public abstract class BaseEmailServiceTest {

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

  @Container
  private static GenericContainer<?> MAILHOG_CONTAINER = new GenericContainer<>(MAILHOG_IMAGE_NAME)
      .withExposedPorts(PORT_SMTP, PORT_HTTP);

  public static void setupMailProperties(DynamicPropertyRegistry registry) {
    registry.add("tc.mailhog.http", () -> MAILHOG_CONTAINER.getMappedPort(PORT_HTTP));
    registry.add("mail.host", MAILHOG_CONTAINER::getHost);
    registry.add("mail.port", () -> MAILHOG_CONTAINER.getMappedPort(PORT_SMTP));
  }

  public final ResponseEntity<String> lookupSentEmailsContaining(String query) {
    RestTemplate rt = new RestTemplate();
    ResponseEntity<String> events = rt.getForEntity(
        String.format("http://%s:%d/api/v2/search?kind=containing&query=" + query, this.host,
            this.mailhogHttpPort),
        String.class);
    log.info("events {}", events);
    return events;
  }

  public final void checkSentEmail(ResponseEntity<String> events, String expectedSubject,
      String expectedFrom, String expectedTo, String expectedMessage) {
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
