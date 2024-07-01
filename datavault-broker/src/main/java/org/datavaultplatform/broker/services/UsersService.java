package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    private final UserDAO userDAO;

    @Autowired
    public UsersService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<User> getUsers() {
        return userDAO.list();
    }

    public List<User> search(String query) { return this.userDAO.search(query); }
    
    public void addPolicy(User user) {
        userDAO.save(user);
    }
    
    public void updateUser(User user) {
        userDAO.update(user);
    }
    
    public User getUser(String userID) {
        return userDAO.findById(userID).orElse(null);
    }

    public long count() { return userDAO.count(); }

    public void addUser(User user) {
        userDAO.save(user);
    }

    public Boolean validateUser(String userID, String password) {
        User user;

        user = userDAO.findById(userID).orElse(null);

        if (user == null) {
            // No match was found
            return false;
        }

        if (user.getPassword() == null) {
            // We are not using passwords so its tickety boo!
            return true;
        }

        // Passwords match so its tickety boo!
        return password.equals(user.getPassword());
    }
}

