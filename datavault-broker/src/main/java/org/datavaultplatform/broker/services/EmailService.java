package org.datavaultplatform.broker.services;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class EmailService {
    
    private MailSender mailSender;
    private String mailAdministrator;
    
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void setMailAdministrator(String mailAdministrator) {
        this.mailAdministrator = mailAdministrator;
    }
    
    public void sendMail(String to, String subject, String message) {
        
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
}
