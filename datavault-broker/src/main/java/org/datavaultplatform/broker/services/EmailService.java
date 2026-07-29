package org.datavaultplatform.broker.services;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.email.EmailBodyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.datavaultplatform.common.model.User;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.util.Map;


@Service
@Slf4j
public class EmailService {

    private final UsersService usersService;

    private final JavaMailSender mailSender;
    private final String mailAdministrator;

    private final EmailBodyGenerator emailBodyGenerator;

    /*
        <bean id="emailService" class="org.datavaultplatform.broker.services.EmailService">
        <property name="mailSender" ref="mailSender"/>
        <property name="usersService" ref="usersService" />
        <property name="velocityEngine" ref="velocityEngine"/>
        <property name="mailAdministrator" value="${mail.administrator}" />
        </bean>
     */
    @Autowired
    public EmailService(UsersService usersService,
        JavaMailSender mailSender,
        EmailBodyGenerator emailBodyGenerator,
        @Value("${mail.administrator}") String mailAdministrator) {
        this.usersService = usersService;
        this.mailSender = mailSender;
        this.emailBodyGenerator = emailBodyGenerator;
        this.mailAdministrator = mailAdministrator;
    }

    public UsersService getUsersService() { return usersService; }


    // Send a plain-text email
    public void sendPlaintextMail(final String to, final String subject, final String message) {
        if (!User.isValidEmail(to)) {
            log.error("Invalid Email [{}]", to);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setFrom(mailAdministrator);
        msg.setText(message);
        msg.setSubject(subject);
        
        try {
            mailSender.send(msg);
        } catch (MailException ex) {
            log.error("problem sending email",ex);
        }
    }

    public void sendTemplateMailToUser(String userID, final String subject, final String template, final Map<String,Object> model) {
        User user = usersService.getUser(userID);
        sendTemplateMailToUser(user, subject, template, model);
    }

    public void sendTemplateMailToUser(User user, final String subject, final String template, final Map<String,Object> model) {
        if (user.isValidEmail()) {
            sendTemplateMail(user.getEmail(),
                    subject,
                    template,
                    model);
        } else {
            // TODO: email admin "User missing email"
            log.error("Email Invalid for UserId[{}]Email[{}].  Can't send email", user.getID(), user.getEmail());
        }
    }

    // Send an HTML email based on a Velocity template using data from the provided model
    public void sendTemplateMail(final String to, final String subject, final String template, final Map<String,Object> model) {
        if (!User.isValidEmail(to)) {
            log.error("Invalid Email [{}]", to);
            return;
        }
        String text = emailBodyGenerator.generate(template, model);
        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
            message.setTo(to);
            message.setFrom(mailAdministrator);
            message.setSubject(subject);
            message.setText(text, true);
        };
        
        try {
            this.mailSender.send(preparator);
        } catch (MailException ex) {
            log.error("problem sending email", ex);
        }
    }
}
