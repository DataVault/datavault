package org.datavaultplatform.worker.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActuatorHelperController {

    @GetMapping("/actuator/")
    public String actuatorWithTrailingSlash(){
        return "forward:/actuator";
    }
}
