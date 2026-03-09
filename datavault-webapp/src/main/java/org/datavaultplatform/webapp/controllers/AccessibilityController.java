package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.webapp.services.RestService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@ConditionalOnBean(RestService.class)
public class AccessibilityController implements AccessibilityControllerApi {

    public AccessibilityController() {
    }

    @Override
    @GetMapping(value = "/accessibility", produces = MediaType.TEXT_HTML_VALUE)
    public String accessibility() {
        return "accessibility";
    }
}
