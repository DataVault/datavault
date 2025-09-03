package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@ConditionalOnBean(RestService.class)
public class AccessibilityController {

    @Autowired
    public AccessibilityController() {
    }

    @RequestMapping(value = "/accessibility", method = RequestMethod.GET)
    public String accessibility() {
        return "accessibility";
    }
}
