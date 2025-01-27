package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.webapp.services.RestService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * User: Stuart Lewis
 * Date: 6/10/2015
 */

@Controller
@ConditionalOnBean(RestService.class)
public class AdminRetrievesController {

    private final RestService restService;
    private static final int DEFAULT_RECORDS_PER_PAGE = 10;

    public AdminRetrievesController(RestService restService) {
        this.restService = restService;
    }

    @RequestMapping(value = "/admin/retrieves", method = RequestMethod.GET)
    public String getRetrievesListing(ModelMap model,
                                      @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                      @RequestParam(value = "sort", required = false, defaultValue = "timestamp") String sort,
                                      @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
                                      @RequestParam(value = "pageId", defaultValue = "1") int pageId) throws Exception {
        model.addAttribute("activePageId", pageId);

        // calculate offset which is passed to the service to fetch records from that row Id
        int offset = (pageId-1) * DEFAULT_RECORDS_PER_PAGE;

        model.addAttribute("offset", offset);

        //model.addAttribute("retrieves", restService.getRetrievesListingAll());
        Retrieve[] retrieves = restService.getRetrievesListingAll(query, sort, order, offset, DEFAULT_RECORDS_PER_PAGE);

        int totalRetrieves = restService.getTotalRetrievesCount(query);

        int numberOfPages = (int)Math.ceil((double) retrieves.length/DEFAULT_RECORDS_PER_PAGE);

        model.addAttribute("query", "");
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("retrieves", retrieves);
        model.addAttribute("query", query);
        model.addAttribute("recordPerPage", DEFAULT_RECORDS_PER_PAGE);
        model.addAttribute("totalRecords", totalRetrieves);
        model.addAttribute("totalPages", (int)Math.ceil((double)totalRetrieves/DEFAULT_RECORDS_PER_PAGE));

        return "admin/retrieves/index";
    }
}


