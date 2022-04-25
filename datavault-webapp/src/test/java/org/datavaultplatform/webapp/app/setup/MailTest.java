package org.datavaultplatform.webapp.app.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.datavaultplatform.webapp.config.MailConfig.MessageCreator;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 Uses docker image mailhog/mailhob to test emails have been sent correctly.
 See https://github.com/mailhog/MailHog
 See https://hub.docker.com/r/mailhog/mailhog
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ProfileStandalone
@Testcontainers
public class MailTest {

  public static final String MAILHOG_IMAGE_NAME = "mailhog/mailhog:v1.0.1";

  public static final int PORT_SMTP = 1025;
  public static final int PORT_HTTP = 8025;

  @Value("${tc.mailhog.http}")
  int mailhogHttpPort;

  @Autowired
  JavaMailSender sender;

  @Value("${mail.host}")
  String host;

  @Autowired
  MessageCreator messageCreator;

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
  void testMailRandomMessage() {
    String rand = UUID.randomUUID().toString();
    String subject = "sub-" + rand;
    String text = "text-" + rand;
    String from = "test.sender@test.com";
    String to = "test.recvr@test.com";

    SimpleMailMessage message = new SimpleMailMessage();
    message.setSubject(subject);
    message.setText(text);
    message.setFrom(from);
    message.setTo(to);

    sender.send(message);

    ResponseEntity<String> events = lookupSentEmailsContaining(text);

    checkSentEmail(events, subject, from, to, text);
  }

  @Test
  void testTemplateMessage() {

    String messageText = UUID.randomUUID().toString();

    SimpleMailMessage message = messageCreator.createMailMessage(messageText);

    sender.send(message);

    ResponseEntity<String> events = lookupSentEmailsContaining(messageText);

    checkSentEmail(events, "DataVault feedback", "feedback@datavaultplatform.org", "feedback@datavaultplatform.org", messageText);
  }

  private ResponseEntity<String> lookupSentEmailsContaining(String query) {
    RestTemplate rt = new RestTemplate();
    ResponseEntity<String> events = rt.getForEntity(
        String.format("http://%s:%d/api/v2/search?kind=containing&query=" + query, this.host,
            this.mailhogHttpPort),
        String.class);
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
    assertEquals(expectedMessage, actualMessage);
    assertEquals(expectedSubject, actualSubject);
    assertEquals(expectedTo, actualTo);
  }

}
