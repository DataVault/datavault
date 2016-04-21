package org.datavaultplatform.webapp.controllers;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * User: Stuart Lewis
 * Date: 27/02/2016
 */
@Controller
public class HelpController {

    private String system;
    private String link;

    public void setSystem(String system) {
        this.system = system;
    }
    public void setLink(String link) {
        this.link = link;
    }

    @RequestMapping(value = "/help", method = RequestMethod.GET)
    public String help(ModelMap model) {

        model.put("system", system);
        model.put("link", link);

        return "help/index";
    }
}
