package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.dao.GroupDAO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupsService {

    private final GroupDAO groupDAO;

    public GroupsService(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public List<Group> getGroups() {
        return groupDAO.list();
    }

    public List<Group> getGroups(String userId) {
        return groupDAO.list(userId);
    }

    public void addGroup(Group group) {
        
        // Default required "enabled" property
        group.setEnabled(Boolean.TRUE);
        
        groupDAO.save(group);
    }

    public void updateGroup(Group group) {
        groupDAO.update(group);
    }

    public void deleteGroup(Group group) {
        groupDAO.delete(group);
    }

    public Group getGroup(String groupID) {
        return groupDAO.findById(groupID).orElse(null);
    }

    public int count(String userId) { return groupDAO.count(userId); }
}

