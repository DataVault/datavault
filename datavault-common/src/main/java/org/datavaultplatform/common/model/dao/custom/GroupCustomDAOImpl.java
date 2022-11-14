package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Group_;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.dao.SchoolPermissionQueryHelper;
import org.datavaultplatform.common.util.DaoUtils;


public class GroupCustomDAOImpl extends BaseCustomDAOImpl implements GroupCustomDAO {

    public GroupCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<Group> list(String userId) {
        SchoolPermissionQueryHelper<Group> helper = createGroupQueryHelper(userId, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        if (helper.hasNoAccess()) {
            return new ArrayList<>();
        }
        helper.setOrderByHelper((cb,rt)->
            Collections.singletonList(cb.asc(rt.get(Group_.NAME))));
        List<Group> groups = helper.getItems();
        return groups;
    }

    @Override
    public int count(String userId) {
        SchoolPermissionQueryHelper<Group> helper = createGroupQueryHelper(userId, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        if (helper.hasNoAccess()) {
            return 0;
        }
        return helper.getItemCount().intValue();
    }

    private SchoolPermissionQueryHelper<Group> createGroupQueryHelper(String userId, Permission permission) {
        return new SchoolPermissionQueryHelper<>(em, Group.class)
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(em, userId, permission));
    }
}
