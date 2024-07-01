package org.datavaultplatform.broker.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActuatorHelperController {

    @GetMapping("/actuator/")
    String ActuatorWithTrailingSlash(){
        return "forward:/actuator";
    }
}

