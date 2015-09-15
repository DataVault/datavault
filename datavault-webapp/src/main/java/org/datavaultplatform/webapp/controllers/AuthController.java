package org.datavaultplatform.webapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage(@RequestParam(value="error", required=false) boolean error, ModelMap model) {
        
        if (error == true) {
            // Login failed
            model.put("error", "Invalid username or password!");
        } else {
            model.put("error", "");
        }
        
        return "/auth/login";
 }
    
    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        
        return "/auth/denied";
    }
}