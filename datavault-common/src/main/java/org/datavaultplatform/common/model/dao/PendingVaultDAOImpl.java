package org.datavaultplatform.common.model.dao;

import java.util.ArrayList;
import java.util.List;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class PendingVaultDAOImpl implements PendingVaultDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingVaultDAOImpl.class);

    private final SessionFactory sessionFactory;

    public PendingVaultDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void save(PendingVault pendingVault) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(pendingVault);
        tx.commit();
        session.close();
    }

    @Override
    public PendingVault findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PendingVault.class);
        criteria.add(Restrictions.eq("id", Id));
        PendingVault vault = (PendingVault)criteria.uniqueResult();
        session.close();
        return vault;
    }

    @Override
    public void update(PendingVault vault) {
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(vault);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("PendingVault.update - ROLLBACK");
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<PendingVault> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PendingVault.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("creationTime"));
        List<PendingVault> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PendingVault> list(String userId, String sort, String order, String offset, String maxResult) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createPendingVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        order(sort, order, criteria);
        
        if((offset != null && maxResult != null) || !maxResult.equals("0")) {
        	criteria.setMaxResults(Integer.valueOf(maxResult));
        	criteria.setFirstResult(Integer.valueOf(offset));
        }

        List<PendingVault> vaults = criteria.list();
        session.close();
        return vaults;
    }
    
    @Override
	public int getTotalNumberOfPendingVaults(String userId, String confirmed) {
		Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createPendingVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        if (confirmed != null && ! confirmed.equals("null") && ! confirmed.equals("")){
            Boolean conf = new Boolean(confirmed);
            criteria.add(Restrictions.eq("confirmed", conf));
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        int totalNumberOfVaults = ((Long) criteria.uniqueResult()).intValue();
        session.close();
        return totalNumberOfVaults;
	}

	/**
	 * Retrieve Total NUmber of rows after applying the filter
	 */
	@Override
	public int getTotalNumberOfPendingVaults(String userId, String query, String confirmed) {
		Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createPendingVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        if (query != null && !query.equals("")) {
            criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("name", "%" + query + "%"), Restrictions.ilike("description", "%" + query + "%")));
        }
        if (confirmed != null && ! confirmed.equals("null") && ! confirmed.equals("")){
            Boolean conf = new Boolean(confirmed);
            criteria.add(Restrictions.eq("confirmed", conf));
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        int totalNumberOfVaults = ((Long) criteria.uniqueResult()).intValue();
        session.close();
        return totalNumberOfVaults;
	}
    
    @SuppressWarnings("unchecked")
    @Override
    public List<PendingVault> search(String userId, String query, String sort, String order, String offset, String maxResult, String confirmed) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createPendingVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        if( ! (query == null || query.equals("")) ) {
            criteria.add(Restrictions.or(
                    Restrictions.ilike("id", "%" + query + "%"),
                    Restrictions.ilike("name", "%" + query + "%"),
                    Restrictions.ilike("description", "%" + query + "%")));
        }

        if (confirmed != null && ! confirmed.equals("null") && ! confirmed.equals("")){
            Boolean conf = new Boolean(confirmed);
            criteria.add(Restrictions.eq("confirmed", conf));
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        order(sort, order, criteria);
        if((offset != null && maxResult != null) || !maxResult.equals("0")) {
        	criteria.setMaxResults(Integer.valueOf(maxResult));
        	criteria.setFirstResult(Integer.valueOf(offset));
        }

        List<PendingVault> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @Override
    public int count(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createPendingVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        return (int) (long) (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }
    
    private SchoolPermissionCriteriaBuilder createPendingVaultCriteriaBuilder(String userId, Session session, Permission permission) {
	    return new SchoolPermissionCriteriaBuilder()
                .setCriteriaType(PendingVault.class)
                .setCriteriaName("pendingvault")
                .setSession(session)
                .setTypeToSchoolAliasGenerator(criteria -> criteria.createAlias("pendingvault.group", "group"))
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(session, userId, permission));
    }
    
    private void order(String sort, String order, Criteria criteria) {
        // Default to ascending order
        boolean asc = ("desc".equals(order))?false:true;

//        // See if there is a valid sort option
        if ("user".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("user.id"));
            } else {
                criteria.addOrder(Order.desc("user.id"));
            }
        } else {
            if (asc) {
                criteria.addOrder(Order.asc(sort));
            } else {
                criteria.addOrder(Order.desc(sort));
            }
        }
    }

    @Override
    public void deleteById(String Id) {

        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(PendingVault.class);
        criteria.add(Restrictions.eq("id", Id));
        PendingVault pv = (PendingVault) criteria.uniqueResult();
        //session.delete(pv);
        //session.flush();
        //session.close();

        Transaction tx = session.beginTransaction();
        session.delete(pv);
        tx.commit();
        session.close();
    }
}
