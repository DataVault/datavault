package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.webapp.config.MailConfig.MessageCreator;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Controller;

/**
 * User: Stuart Lewis
 * Date: 29/11/2015
 */
@Controller
@ConditionalOnBean(RestService.class)
public class FeedbackController {

    private final MailSender mailSender;
    private final MessageCreator messageCreator;

    @Autowired
    public FeedbackController(MailSender mailSender,
        MessageCreator messageCreator) {
        this.mailSender = mailSender;
        this.messageCreator = messageCreator;
    }

    /*
     * Replaced by EdWeb ‘Contact’ page
     */
//    @RequestMapping(value = "/feedback", method = RequestMethod.GET)
//    public String feedback(ModelMap model) {
//
//        return "feedback/index";
//    }
//
//    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
//    public String sendFeedback(ModelMap model,
//                               @RequestParam String email,
//                               @RequestParam String feedback,
//                               @RequestParam String action) {
//        // Was the cancel button pressed?
//        if ("cancel".equals(action)) {
//            return "redirect:/";
//        }
//
//        // Send the feedback
//        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
//        msg.setText("Feedback from: " + email + "\n" +
//                    "Message: " + feedback);
//        try{
//            this.mailSender.send(msg);
//        }
//        catch (MailException ex) {
//            // TODO better logging
//            System.err.println(ex.getMessage());
//        }
//
//        return "feedback/sent";
//    }

}
