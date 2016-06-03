package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.FileStore;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * User: Robin Taylor
 * Date: 27/11/2015
 * Time: 11:00
 */
@Controller
public class FileStoreController {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreController.class);

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }


    // Return an 'add keys' page
    @RequestMapping(value = "/users/{userid}/keys", method = RequestMethod.GET)
    public String addKeys(ModelMap model, @PathVariable("userid") String userID) {

        model.addAttribute("keysExist", restService.keysExist(userID));
        model.addAttribute("user", restService.getUser(userID));
        return "users/addKeys";
    }

    // Process the completed 'add keys' page
    @RequestMapping(value = "/users/{userid}/keys", method = RequestMethod.POST)
    public String addKeys(@ModelAttribute User user, ModelMap model, @PathVariable("userid") String userID, @RequestParam String action) {
        // Was the cancel button pressed?
        if ("cancel".equals(action)) {
            return "redirect:/";
        }

        model.addAttribute("publicKey", restService.addKeys(userID));
        model.addAttribute("user", restService.getUser(userID));

        return "users/listKeys";
    }


    // Process the 'add local FileStore' Ajax request
    @RequestMapping(value = "/filestores/local", method = RequestMethod.POST)
    @ResponseBody
    public String addLocalFilestore(@RequestParam String dirname, @RequestParam String action) {
        HashMap<String,String> storeProperties = new HashMap<String,String>();
        storeProperties.put("rootPath", dirname);
        FileStore store = new FileStore("org.datavaultplatform.common.storage.impl.LocalFileSystem", storeProperties, "Filesystem (local)");
        restService.addFileStore(store);

        return null;
    }

    // Process the 'add keys' Ajax request
    @RequestMapping(value = "/filestores/keys", method = RequestMethod.POST)
    @ResponseBody
    public String addKeys(@RequestParam String action) {
        String publicKey = restService.addKeys();

        return publicKey;
    }




}
