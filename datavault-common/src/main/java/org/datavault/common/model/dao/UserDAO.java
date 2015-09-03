package org.datavault.common.model.dao;

import java.util.List;
import org.datavault.common.model.User;
 
public interface UserDAO {

    public void save(User user);
    
    public void update(User user);
    
    public List<User> list();

    public User findById(String Id);
}
