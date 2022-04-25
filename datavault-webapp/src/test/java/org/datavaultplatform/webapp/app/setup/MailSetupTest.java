package org.datavaultplatform.webapp.app.setup;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.config.MailConfig.MessageCreator;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestPropertySource;

/* We override this property to test better.
In the default config 'mail.administrator' has same value as 'mail.from',
this is not great for testing.
*/
@SpringBootTest
@Slf4j
@ProfileStandalone
@TestPropertySource(properties = "mail.administrator=mail.admin@test.com")
public class MailSetupTest {

  @Autowired
  @Qualifier("mailSender")
  JavaMailSenderImpl mailSender;

  @Autowired
  MessageCreator messageCreator;

  @Test
  void testMailSenderConfig(){

      assertThat(mailSender.getProtocol()).isEqualTo("smtp");
      assertThat(mailSender.getHost()).isEqualTo("smtp.gmail.com");
      assertThat(mailSender.getPort()).isEqualTo(587);

      Properties props = mailSender.getJavaMailProperties();
      assertThat(props.getProperty("mail.smtp.auth")).isEqualTo("false");
      assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
      assertThat(props.getProperty("mail.smtp.quitwait")).isEqualTo("true");

      assertThat(props.size()).isEqualTo(3);
  }

  @Test
  void testMailMessageConfig() {
    SimpleMailMessage message = messageCreator.createMailMessage("blah");
    assertThat(message.getSubject()).isEqualTo("DataVault feedback");
    assertThat(message.getFrom()).isEqualTo("feedback@datavaultplatform.org");
    assertThat(message.getTo().length).isOne();
    assertThat(message.getTo()[0]).isEqualTo("mail.admin@test.com");
    assertThat(message.getText()).isEqualTo("blah");
  }

}
