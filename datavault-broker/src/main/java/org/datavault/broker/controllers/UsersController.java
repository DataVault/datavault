package org.datavault.broker.controllers;

import java.util.List;

import org.datavault.common.model.User;
import org.datavault.broker.services.UsersService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersController {
    
    private UsersService usersService;
    
    public void setUsersService(UsersService usersService) {
        this.usersService = usersService;
    }
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<User> getUsers(@RequestHeader(value = "X-UserID", required = true) String userID) {
        return usersService.getUsers();
    }
    
    @RequestMapping(value = "/users/{userid}", method = RequestMethod.GET)
    public User getUser(@RequestHeader(value = "X-UserID", required = true) String userID, 
                        @PathVariable("userid") String queryUserID) {
        return usersService.getUser(queryUserID);
    }
}
