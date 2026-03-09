package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * User: Stuart Lewis
 * Date: 27/02/2016
 */
@Controller
@ConditionalOnBean(RestService.class)
public class HelpController implements HelpControllerApi {

    public static final String SYSTEM = "system";
    public static final String LINK = "link";
    private final String system;
    private final String link;

    @Autowired
    public HelpController(
        @Value("${metadata.system}") String system,
        @Value("${metadata.link}") String link) {
        this.system = system;
        this.link = link;
    }

    @Override
    @GetMapping(value = "/help", produces = MediaType.TEXT_HTML_VALUE)
    public String help(ModelMap model) {

        model.put(SYSTEM, system);
        model.put(LINK, link);

        return "help/index";
    }
}
