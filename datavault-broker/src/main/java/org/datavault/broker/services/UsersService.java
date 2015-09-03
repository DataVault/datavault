package org.datavault.broker.services;

import org.datavault.common.model.User;
import org.datavault.common.model.dao.UserDAO;

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

