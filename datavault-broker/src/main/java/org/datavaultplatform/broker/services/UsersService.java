package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.UserDAO;

import java.util.List;

public class UsersService {

    private UserDAO userDAO;
    
    public List<User> getUsers() {
        return userDAO.list();
    }
    
    public void addPolicy(User user) {
        userDAO.save(user);
    }
    
    public void updateUser(User user) {
        userDAO.update(user);
    }
    
    public User getUser(String userID) {
        return userDAO.findById(userID);
    }
    
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}

