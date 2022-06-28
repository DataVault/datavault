package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: Stuart Lewis
 * Date: 27/02/2016
 */
@Controller
@ConditionalOnBean(RestService.class)
public class HelpController {

    private final String system;
    private final String link;

    @Autowired
    public HelpController(
        @Value("${metadata.system}") String system,
        @Value("${metadata.link}") String link) {
        this.system = system;
        this.link = link;
    }

    @RequestMapping(value = "/help", method = RequestMethod.GET)
    public String help(ModelMap model) {

        model.put("system", system);
        model.put("link", link);

        return "help/index";
    }
}
