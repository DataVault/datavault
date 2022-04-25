package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@ConditionalOnBean(RestService.class)
public class AdminEventsController {

    private final RestService restService;

    @Autowired
    public AdminEventsController(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/events", method = RequestMethod.GET)
    public String getEventsListing(ModelMap model,
                                   @RequestParam(value = "query", required = false) String query,
                                   @RequestParam(value = "sort", required = false) String sort) throws Exception {
        
        model.addAttribute("events", restService.getEvents());
        
        return "admin/events/index";
    }
}


