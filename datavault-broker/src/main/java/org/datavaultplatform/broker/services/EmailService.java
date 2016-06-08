package org.datavaultplatform.broker.services;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.apache.velocity.app.VelocityEngine;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;

public class EmailService {
    
    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private String mailAdministrator;
    
    private final String encoding = "UTF-8";
    
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }
    
    public void setMailAdministrator(String mailAdministrator) {
        this.mailAdministrator = mailAdministrator;
    }
    
    // Send a plain-text email
    public void sendPlaintextMail(final String to, final String subject, final String message) {
        
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setFrom(mailAdministrator);
        msg.setText(message);
        msg.setSubject(subject);
        
        try {
            mailSender.send(msg);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    // Send an HTML email based on a Velocity template using data from the provided model
    public void sendTemplateMail(final String to, final String subject, final String template, final HashMap model) {

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setTo(to);
                message.setFrom(mailAdministrator);
                message.setSubject(subject);
                String text = VelocityEngineUtils.mergeTemplateIntoString(
                        velocityEngine, template, encoding, model);
                message.setText(text, true);
            }
        };
        
        try {
            this.mailSender.send(preparator);
        } catch (MailException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
