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
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.util.UsesTestContainers;
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

/*
 Uses docker image mailhog/mailhob to test emails have been sent correctly.
 See https://github.com/mailhog/MailHog
 See https://hub.docker.com/r/mailhog/mailhog
 */
@SpringBootTest(classes = DataVaultBrokerApp.class)
@UsesTestContainers
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.services.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.database.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.initialise.enabled=false",
})
@AddTestProperties
@TestPropertySource(properties = {
    "spring.jpa.properties.hibernate.hbm2ddl.auto=none",
    "broker.scheduled.enabled=false",
    "broker.rabbit.enabled=false",
    "mail.administrator=test@datavaultplatform.org",
    "spring.sql.init.mode=never"})
@Import(EmailService.class)
@Slf4j
public abstract class BaseEmailServiceTest {

  public static final String EXPECTED_FROM = "test@datavaultplatform.org";

  @MockBean
  DataSource datasource;

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
  FileStoreService mFileStoreService;

  @MockBean
  RolesAndPermissionsService rolesAndPermissionsService;

  @MockBean
  ArchiveStoreService mArchiveStoreService;

  @Autowired
  EmailService emailService;

  private static final Map<String, Object> TEMPLATE_MODEL = new HashMap<>() {{
    put("audit-id", "aud-id-001");
    put("timestamp", "timestamp-002");
    put("chunk-id", "chunk-id-003");
    put("archive-id", "archive-id-004");
    put("location", "location-005");
  }};

  String EXPECTED_EMAIL_MSG_BODY = """
          <html>
          \t<body>
          \t\t
          \t\t<p>The attempted audit of the following chunk failed:</p>
          \t\t<ul>
          \t\t\t<li>Audit ID: aud-id-001</li>
          \t\t\t<li>Date of failed operation: timestamp-002</li>
          \t\t\t<li>Chunk ID: chunk-id-003</li>
          \t\t\t<li>Archive ID: archive-id-004</li>
          \t\t\t<li>location: location-005</li>
          \t\t</ul>
          \t</body>
          </html>""";

  @Container
  private static final GenericContainer<?> MAILHOG_CONTAINER
      = new GenericContainer<>(DockerImage.MAIL_IMAGE).withExposedPorts(PORT_SMTP, PORT_HTTP);

  public static void setupMailProperties(DynamicPropertyRegistry registry) {
    registry.add("tc.mailhog.http", () -> MAILHOG_CONTAINER.getMappedPort(PORT_HTTP));
    registry.add("mail.host", MAILHOG_CONTAINER::getHost);
    registry.add("mail.port", () -> MAILHOG_CONTAINER.getMappedPort(PORT_SMTP));
    log.info("email http://localhost:{}", MAILHOG_CONTAINER.getMappedPort(PORT_HTTP));
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
