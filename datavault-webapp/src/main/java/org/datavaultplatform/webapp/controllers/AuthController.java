package org.datavaultplatform.webapp.controllers;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage(@RequestParam(value="error", required=false) boolean error,
                               @RequestParam(value="logout", required=false) String logout, ModelMap model) {

        model.put("success", "");
        model.put("error", "");
        if (logout != null) {
            // Logout
            model.put("success", "You are now logged out");
        }if (error == true) {
            // Login failed
            model.put("error", "Invalid username or password!");
        }
        
        return "/auth/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String getDeniedPage(ModelMap model, HttpSession session) {

        session.invalidate();
        return "redirect:/auth/login?logout";
    }

    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {

        return "/auth/denied";
    }
}