package org.datavaultplatform.webapp.controllers.admin;


import org.datavaultplatform.webapp.services.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@ConditionalOnBean(RestService.class)
public class AdminEventsController implements AdminEventsControllerApi {

    private final RestService restService;

    @Autowired
    public AdminEventsController(RestService restService) {
        this.restService = restService;
    }

    @Override
    @GetMapping(value = "/admin/events", produces = MediaType.TEXT_HTML_VALUE)
    public String getEventsListing(ModelMap model,
                                   @RequestParam(value = "query", required = false) String query,
                                   @RequestParam(value = "sort", required = false) String sort) throws Exception {
        
        model.addAttribute("events", restService.getEvents());
        
        return "admin/events/index";
    }
}


