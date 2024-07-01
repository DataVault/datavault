package org.datavaultplatform.broker.controllers;

import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.pojo.ApiVerb;
import org.jsondoc.core.pojo.ApiVisibility;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@Api(name="Index", description = "Show JSONDOC page", visibility = ApiVisibility.PRIVATE)
public class IndexController {

    @ApiMethod(
            path = "/",
            verb = ApiVerb.GET,
            description = "Display the JSONDOC homepage",
            responsestatuscode = "200 - OK"
    )
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexPage(ModelMap model, HttpServletRequest servletRequest, HttpServletResponse response) {
        return "resources/index.html";
    }
}
