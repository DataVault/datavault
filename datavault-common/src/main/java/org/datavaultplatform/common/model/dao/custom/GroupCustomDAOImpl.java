package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.dao.SchoolPermissionCriteriaBuilder;
import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;


public class GroupCustomDAOImpl extends BaseCustomDAOImpl implements GroupCustomDAO {

    public GroupCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<Group> list(String userId) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createGroupCriteriaBuilder(userId, session, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        List<Group> groups = criteria.addOrder(Order.asc("name")).list();
        return groups;
    }

    @Override
    public int count(String userId) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createGroupCriteriaBuilder(userId, session, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        Long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return count.intValue();
    }

    private SchoolPermissionCriteriaBuilder createGroupCriteriaBuilder(String userId, Session session, Permission permission) {
        return new SchoolPermissionCriteriaBuilder()
                .setCriteriaType(Group.class)
                .setCriteriaName("group")
                .setSession(session)
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(session, userId, permission));
    }
}
