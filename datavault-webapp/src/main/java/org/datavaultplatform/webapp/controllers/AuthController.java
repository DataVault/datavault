package org.datavaultplatform.webapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    private final static String DEFAULT_LOGOUT_URL = "/auth/login?logout";
    
    private String welcome;
    private String logoutUrl;
    
    public void setWelcome(String welcome) {
        this.welcome = welcome;
    }
    
    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage(@RequestParam(value="error", required=false) boolean error,
                               @RequestParam(value="logout", required=false) String logout,
                               @RequestParam(value="security", required=false) String security,
                               ModelMap model) {

        model.put("success", "");
        model.put("error", "");
        model.put("welcome", welcome);
        
        if (logout != null) {
            // Logout
            model.put("success", "You are now logged out");
        }
        if (error) {
            // Login failed
            model.put("error", "Invalid username or password!");
        } else if (security != null) {
            // Permissions changed
            model.put("error", "You have been logged out for security reasons. Please log back in to continue");
        }
        
        return "/auth/login";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String getDeniedPage(ModelMap model, HttpSession session) {

        session.invalidate();
        
        if (logoutUrl == null || logoutUrl.equals("")) {
            logoutUrl = AuthController.DEFAULT_LOGOUT_URL;
        }
        
        return "redirect:"+logoutUrl;
    }

    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {

        return "/auth/denied";
    }

    @RequestMapping(value = "/confirmation", method = RequestMethod.GET)
    public String getConfirmationPage(ModelMap model) {

        model.put("logout", "");

        return "/auth/confirmation";
    }
}