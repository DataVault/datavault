package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.User;
 
public interface UserDAO {

    public void save(User user);
    
    public void update(User user);
    
    public List<User> list();

    public User findById(String Id);

    public List<User> search(String query);

    public int count();
}
