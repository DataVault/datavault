package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.dao.GroupDAO;

import java.util.List;

public class GroupsService {

    private GroupDAO groupDAO;

    public List<Group> getGroups() {
        return groupDAO.list();
    }

    public void addPolicy(Group group) {
        groupDAO.save(group);
    }

    public void updateUser(Group group) {
        groupDAO.update(group);
    }

    public Group getGroup(String groupID) {
        return groupDAO.findById(groupID);
    }

    public void setGroupDAO(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public int count() { return groupDAO.count(); }

    public int vaultCount(String groupId) {
        return groupDAO.countVaultsById(groupId);
    }
}

