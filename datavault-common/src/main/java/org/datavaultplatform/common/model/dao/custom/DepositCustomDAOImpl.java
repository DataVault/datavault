package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.dao.SchoolPermissionCriteriaBuilder;
import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class DepositCustomDAOImpl extends BaseCustomDAOImpl implements DepositCustomDAO {

    public DepositCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> list(String query,  String userId, String sort, String order, int offset, int maxResult) {
        System.out.println("query:"+query+", sort: "+sort+", order: "+order+", offset: "+offset+", maxResult: "+maxResult);
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();

        System.out.println("DAO.list query="+query);
        if((query == null || "".equals(query)) == false) {
            System.out.println("apply restrictions");
            criteria.add(Restrictions.or(
                    Restrictions.ilike("id", "%" + query + "%"),
                    Restrictions.ilike("name", "%" + query + "%")));
        }

        // Sort by creation time by default
        if(sort == null || "".equals(sort)) sort = "creationTime";
        if(maxResult == 0) maxResult = 10;

        if("userID".equals(sort)) sort = "user.id";
        if("vaultID".equals(sort)) sort = "vault.id";

        if ("asc".equals(order)) {
            criteria.addOrder(Order.asc(sort));
        } else {
            criteria.addOrder(Order.desc(sort));
        }
        criteria.setMaxResults(maxResult);
        criteria.setFirstResult(offset);

        List<Deposit> deposits = criteria.list();
        return deposits;
    }

    @Override
    public int count(String userId, String query) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();

        if((query == null || "".equals(query)) == false) {
            System.out.println("apply restrictions");
            criteria.add(Restrictions.or(
                    Restrictions.ilike("id", "%" + query + "%"),
                    Restrictions.ilike("name", "%" + query + "%")));
        }
        Long count =  (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return count.intValue();
    }

    @Override
    public int queueCount(String userId) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_VIEW_QUEUES);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.eq("status", Deposit.Status.NOT_STARTED));
        criteria.setProjection(Projections.rowCount());
        Long count = (Long) criteria.uniqueResult();
        return count.intValue();
    }

    @Override
    public int inProgressCount(String userId) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_VIEW_IN_PROGRESS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.and(Restrictions.ne("status", Deposit.Status.NOT_STARTED), Restrictions.ne("status", Deposit.Status.COMPLETE)));
        criteria.setProjection(Projections.rowCount());
        Long count = (Long) criteria.uniqueResult();
        return count.intValue();
    }

    @Override
    public List<Deposit> inProgress() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.and(Restrictions.ne("status", Deposit.Status.NOT_STARTED), Restrictions.ne("status", Deposit.Status.COMPLETE)));
        List<Deposit> deposits = criteria.list();
        return deposits;
    }

    @Override
    public List<Deposit> completed() {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("status", Deposit.Status.COMPLETE));
        List<Deposit> deposits = criteria.list();
        return deposits;
    }

    @Override
    public List<Deposit> search(String query, String sort, String order, String userId) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_MANAGE_DEPOSITS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.or(
                Restrictions.ilike("id", "%" + query + "%"),
                Restrictions.ilike("name", "%" + query + "%"),
                Restrictions.ilike("filePath", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        // See if there is a valid sort option

        if("asc".equals(order)){
            criteria.addOrder(Order.asc(sort));
        } else {
            criteria.addOrder(Order.desc(sort));
        }

        List<Deposit> deposits = criteria.list();
        return deposits;
    }

    @Override
    public Long size(String userId) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createDepositCriteriaBuilder(userId, session, Permission.CAN_VIEW_VAULTS_SIZE);
        if (criteriaBuilder.hasNoAccess()) {
            return 0L;
        }
        Criteria criteria = criteriaBuilder.build();
        return (Long) criteria.setProjection(Projections.sum("depositSize")).uniqueResult();
    }

    private SchoolPermissionCriteriaBuilder createDepositCriteriaBuilder(String userId, Session session, Permission permission) {
        return new SchoolPermissionCriteriaBuilder()
                .setCriteriaType(Deposit.class)
                .setCriteriaName("deposit")
                .setSession(session)
                .setTypeToSchoolAliasGenerator(criteria ->
                        criteria.createAlias("deposit.vault", "vault")
                                .createAlias("vault.group", "group"))
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(session, userId, permission));
    }

    @Override
    public List<Deposit> getDepositsWaitingForAudit(Date olderThanDate) {
        Session session = this.getCurrentSession();
        Criteria criteria = session.createCriteria(Deposit.class);

        criteria.add(Restrictions.le("creationTime", olderThanDate));
        criteria.add(Restrictions.le("status", Deposit.Status.COMPLETE));

        criteria.addOrder(Order.asc("creationTime"));

        List<Deposit> deposits = criteria.list();
        return deposits;
    }
}
