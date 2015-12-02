package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * User: Stuart Lewis
 * Date: 29/11/2015
 */
@Controller
public class FeedbackController {

    private MailSender mailSender;
    private SimpleMailMessage templateMessage;

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setTemplateMessage(SimpleMailMessage templateMessage) {
        this.templateMessage = templateMessage;
    }

    @RequestMapping(value = "/feedback", method = RequestMethod.GET)
    public String feedback(ModelMap model) {

        return "feedback/index";
    }

    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
    public String sendFeedback(ModelMap model,
                               @RequestParam String email,
                               @RequestParam String feedback,
                               @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        // Send the feedback
        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
        msg.setText("Feedback from: " + email + "\n" +
                    "Message: " + feedback);
        try{
            this.mailSender.send(msg);
        }
        catch (MailException ex) {
            // TODO better logging
            System.err.println(ex.getMessage());
        }

        return "feedback/sent";
    }

}
