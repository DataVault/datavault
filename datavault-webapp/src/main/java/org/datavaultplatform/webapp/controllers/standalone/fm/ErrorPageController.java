package org.datavaultplatform.webapp.controllers.standalone.fm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
@Profile("standalone")
class ErrorPageController {

    private static final Logger logger = LoggerFactory.getLogger(ErrorPageController.class);

    @GetMapping("/errorpage")
    public String nested(HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("name", "James Bond");
        return "error/error";
    }

}
