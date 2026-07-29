package org.datavaultplatform.broker.services;

import ch.qos.logback.classic.spi.ILoggingEvent;
import jakarta.mail.Address;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.datavaultplatform.broker.email.EmailBodyGenerator;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    String mailAdministrator = "mail.admin@test.com";
    
    @Mock
    UsersService usersService;
    
    @Mock
    JavaMailSender javaMailSender;
    
    @Mock
    EmailBodyGenerator emailBodyGenerator;
    
    EmailService emailService;

    @BeforeEach
    void setup() {
        emailService = new EmailService(this.usersService, this.javaMailSender, this.emailBodyGenerator, this.mailAdministrator);
        assertThat(Mockito.mockingDetails(this.javaMailSender).isMock()).isTrue();
        assertThat(Mockito.mockingDetails(this.usersService).isMock()).isTrue();
        assertThat(Mockito.mockingDetails(this.emailBodyGenerator).isMock()).isTrue();
        assertThat(this.mailAdministrator).isNotBlank();
        assertThat(emailService.getUsersService()).isEqualTo(usersService);
    }

    @Nested
    class SendPlaintextMailTests {
        
        @Captor
        ArgumentCaptor<SimpleMailMessage> argSimpleMailMessage;
        
        @Test
        void testInvalidEmail() {

            emailService.sendPlaintextMail("bob", "subject", "message");
            
            verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
            verifyNoMoreInteractions(javaMailSender, usersService, emailBodyGenerator);
        }

        @Test
        void testMailIsSent() {

            emailService.sendPlaintextMail("bob@example.com", "subject", "message");

            verify(javaMailSender).send(argSimpleMailMessage.capture());
            verifyNoMoreInteractions(javaMailSender, usersService, emailBodyGenerator);
            
            SimpleMailMessage sent = argSimpleMailMessage.getValue();
            assertThat(sent.getTo()).isEqualTo(new String[]{"bob@example.com"});
            assertThat(sent.getFrom()).isEqualTo(mailAdministrator);
            assertThat(sent.getText()).isEqualTo("message");
            assertThat(sent.getSubject()).isEqualTo("subject");
        }

        @Test
        @SneakyThrows
        void testMailIsNotSent() {

            doThrow(new MailException("oops") {}).when(javaMailSender).send(any(SimpleMailMessage.class));
            
            List<ILoggingEvent> events = TestUtils.captureLogging(EmailService.class, () -> {
                emailService.sendPlaintextMail("bob@example.com", "subject", "message");

                verify(javaMailSender).send(argSimpleMailMessage.capture());
                verifyNoMoreInteractions(javaMailSender, usersService, emailBodyGenerator);

                SimpleMailMessage sent = argSimpleMailMessage.getValue();
                assertThat(sent.getTo()).isEqualTo(new String[]{"bob@example.com"});
                assertThat(sent.getFrom()).isEqualTo(mailAdministrator);
                assertThat(sent.getText()).isEqualTo("message");
                assertThat(sent.getSubject()).isEqualTo("subject");
            });
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getFormattedMessage()).isEqualTo("problem sending email");
        }
    }
    
    
    @Nested
    class SendTemplateEmailToUserTests {
        
        @Captor
        ArgumentCaptor<User> argUser;

        @Captor
        ArgumentCaptor<String> argSubject;

        @Captor
        ArgumentCaptor<String> argTemplate;

        @Captor
        ArgumentCaptor<Map<String,Object>> argModel;
        
        @Test
        @SneakyThrows
        void testSendTemplateMailToUserWithUserId(){
            EmailService spy = Mockito.spy(emailService);

            User user = new User();
            user.setID("test-user-id");
            
            when(usersService.getUser("test-user-id")).thenReturn(user);

            doNothing().when(spy).sendTemplateMailToUser(any(User.class), any(String.class), any(String.class), anyMap());
            
            Map<String,Object> model = new HashMap<>();
            spy.sendTemplateMailToUser("test-user-id","test-subject","test-template", model);

            verify(usersService).getUser("test-user-id");
            verify(spy).sendTemplateMailToUser(argUser.capture(), argSubject.capture(), argTemplate.capture(), argModel.capture());
            
            assertThat(argUser.getValue()).isEqualTo(user);
            assertThat(argSubject.getValue()).isEqualTo("test-subject");
            assertThat(argTemplate.getValue()).isEqualTo("test-template");
            assertThat(argModel.getValue()).isEqualTo(model);
        }

        @Test
        void testSendTemplateMailToUserAndValidEmail() {
            EmailService spy = Mockito.spy(emailService);

            User user = new User();
            user.setEmail("bob@test.com");
            
            doNothing().when(spy).sendTemplateMail(any(String.class), any(String.class),any(String.class), anyMap());

            Map<String,Object> model = new HashMap<>();
            spy.sendTemplateMailToUser(user, "test-subject", "test-template", model);
            
            verify(spy).sendTemplateMail("bob@test.com","test-subject","test-template", model);
        }
        
        @Test
        @SneakyThrows
        void testSendTemplateMailToUserAndInValidEmail() {
            EmailService spy = Mockito.spy(emailService);

            User user = new User();
            user.setID("test-user-id");
            user.setEmail("invalid");

            Map<String, Object> model = new HashMap<>();
            List<ILoggingEvent> events = TestUtils.captureLogging(EmailService.class, () -> {
                spy.sendTemplateMailToUser(user, "test-subject", "test-template", model);
            });

            assertThat(events).hasSize(1);
            assertThat(events.get(0).getFormattedMessage()).isEqualTo("Email Invalid for UserId[test-user-id]Email[invalid].  Can't send email");

            verify(spy, never()).sendTemplateMail(any(String.class), any(String.class), any(String.class), anyMap());
        }
    }
    @Nested
    class SendTemplateMailTests {
        
        
        @Captor
        ArgumentCaptor<String> argTemplate;

        @Captor
        ArgumentCaptor<Map<String, Object>> argModel;
        
        @Captor
        ArgumentCaptor<MimeMessagePreparator> argMimeMessagePrep;
        
        @Test
        @SneakyThrows
        void testSendTemplateMailValidEmailSuccess() {

            when(emailBodyGenerator.generate(any(String.class), anyMap()))
                    .thenAnswer(invocation -> "test-generated-body");
            Map<String, Object> model = new HashMap<>();
            emailService.sendTemplateMail("bob@test.com", "test-subject", "test-template", model);    
            
            verify(emailBodyGenerator, times(1)).generate(argTemplate.capture(), argModel.capture());
            assertThat(argTemplate.getValue()).isEqualTo("test-template");
            assertThat(argModel.getValue()).isEqualTo(model);
            
            verify(javaMailSender).send(argMimeMessagePrep.capture());
            MimeMessagePreparator actualPrep = argMimeMessagePrep.getValue();
            MimeMessage mimeMessage = new MimeMessage((Session)null);
            actualPrep.prepare(mimeMessage);
            
            
            //FROM
            Address[] fromAddresses = mimeMessage.getFrom();
            assertThat(fromAddresses).hasSize(1);
            InternetAddress firstFromAddress = (InternetAddress)fromAddresses[0]; 
            assertThat(firstFromAddress.getAddress()).isEqualTo("mail.admin@test.com");
            
            //BODY
            assertThat(mimeMessage.getContent()).isEqualTo("test-generated-body");
            assertThat(mimeMessage.getContentType()).isEqualTo("text/plain");

            //SUBJECT
            assertThat(mimeMessage.getSubject()).isEqualTo("test-subject");

            //TO
            Address[] toAddresses = mimeMessage.getAllRecipients();
            assertThat(toAddresses).hasSize(1);
            InternetAddress firstToAddress = (InternetAddress)toAddresses[0];
            assertThat(firstToAddress.getAddress()).isEqualTo("bob@test.com");
            
            verifyNoMoreInteractions(javaMailSender, usersService, emailBodyGenerator);
        }
        @Test
        @SneakyThrows
        void testSendTemplateMailValidEmailFailure() {

            doThrow(new MailException("oops"){}).when(javaMailSender).send(any(MimeMessagePreparator.class));

            when(emailBodyGenerator.generate(any(String.class), anyMap()))
                    .thenAnswer(invocation -> "test-generated-body");
            Map<String, Object> model = new HashMap<>();
            List<ILoggingEvent> events = TestUtils.captureLogging(EmailService.class, () -> {
                emailService.sendTemplateMail("bob@test.com", "test-subject", "test-template", model);

                verify(emailBodyGenerator, times(1)).generate(argTemplate.capture(), argModel.capture());
                assertThat(argTemplate.getValue()).isEqualTo("test-template");
                assertThat(argModel.getValue()).isEqualTo(model);

                verify(javaMailSender).send(argMimeMessagePrep.capture());
                MimeMessagePreparator actualPrep = argMimeMessagePrep.getValue();
                MimeMessage mimeMessage = new MimeMessage((Session) null);
                actualPrep.prepare(mimeMessage);


                //FROM
                Address[] fromAddresses = mimeMessage.getFrom();
                assertThat(fromAddresses).hasSize(1);
                InternetAddress firstFromAddress = (InternetAddress) fromAddresses[0];
                assertThat(firstFromAddress.getAddress()).isEqualTo("mail.admin@test.com");

                //BODY
                assertThat(mimeMessage.getContent()).isEqualTo("test-generated-body");
                assertThat(mimeMessage.getContentType()).isEqualTo("text/plain");

                //SUBJECT
                assertThat(mimeMessage.getSubject()).isEqualTo("test-subject");

                //TO
                Address[] toAddresses = mimeMessage.getAllRecipients();
                assertThat(toAddresses).hasSize(1);
                InternetAddress firstToAddress = (InternetAddress) toAddresses[0];
                assertThat(firstToAddress.getAddress()).isEqualTo("bob@test.com");

                verifyNoMoreInteractions(javaMailSender, usersService, emailBodyGenerator);
            });
            assertThat(events).hasSize(1);
            assertThat(events.get(0).getFormattedMessage()).isEqualTo("problem sending email");
        }

        @Test
        @SneakyThrows
        void testSendTemplateMailInvalidEmail() {
            Map<String, Object> model = new HashMap<>();

            List<ILoggingEvent> events = TestUtils.captureLogging(EmailService.class, () -> {
                emailService.sendTemplateMail("bob#test.com", "test-subject", "test-template", model);
            });

            assertThat(events).hasSize(1);
            assertThat(events.get(0).getFormattedMessage()).isEqualTo("Invalid Email [bob#test.com]");
            verifyNoMoreInteractions(javaMailSender, usersService, emailBodyGenerator);
        }
    }
}