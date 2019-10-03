package org.datavaultplatform.common.model.dao;

import java.util.ArrayList;
import java.util.List;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultDAOImpl implements VaultDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VaultDAOImpl.class);

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(Vault vault) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(vault);
        tx.commit();
        session.close();
    }
 
    @Override
    public void update(Vault vault) {        
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
                System.out.println("Vault.update - ROLLBACK");
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    @Override
    public void saveOrUpdateVault(Vault vault) {        
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(vault);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                System.out.println("Vault.update - ROLLBACK");
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
    public List<Vault> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("creationTime"));
        List<Vault> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vault> list(String userId, String sort, String order, String offset, String maxResult) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
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

        List<Vault> vaults = criteria.list();
        session.close();
        return vaults;
    }
    
    @Override
    public Vault findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.add(Restrictions.eq("id", Id));
        Vault vault = (Vault)criteria.uniqueResult();
        session.close();
        return vault;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Vault> search(String userId, String query, String sort, String order, String offset, String maxResult) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("name", "%" + query + "%"), Restrictions.ilike("description", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        order(sort, order, criteria);
        if((offset != null && maxResult != null) || !maxResult.equals("0")) {
        	criteria.setMaxResults(Integer.valueOf(maxResult));
        	criteria.setFirstResult(Integer.valueOf(offset));
        }

        List<Vault> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @Override
    public int count(String userId) {
        Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        return (int) (long) (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public int getRetentionPolicyCount(int status) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(Vault.class);
        criteria.add(Restrictions.eq("retentionPolicyStatus", status));
        criteria.setProjection(Projections.rowCount());
        return (int)(long)(Long)criteria.uniqueResult();
    }

    private void order(String sort, String order, Criteria criteria) {
        // Default to ascending order
        boolean asc = false;
        if (!"dec".equals(order)) {
            asc = true;
        }

        // See if there is a valid sort option
        if ("id".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("id"));
            } else {
                criteria.addOrder(Order.desc("id"));
            }
        } else if ("name".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("name"));
            } else {
                criteria.addOrder(Order.desc("name"));
            }
        } else if ("description".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("description"));
            } else {
                criteria.addOrder(Order.desc("description"));
            }
        } else if ("vaultSize".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("vaultSize"));
            } else {
                criteria.addOrder(Order.desc("vaultSize"));
            }
        } else if ("user".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("description"));
            } else {
                criteria.addOrder(Order.desc("description"));
            }
        } else if ("policy".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("policy"));
            } else {
                criteria.addOrder(Order.desc("policy"));
            }
        } else if ("groupID".equals(sort)) {
        	criteria.createAlias("group", "g");
            if (asc) {
                criteria.addOrder(Order.asc("g.id"));
            } else {
                criteria.addOrder(Order.desc("g.id"));
            }
        } else if ("reviewDate".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("reviewDate"));
            } else {
                criteria.addOrder(Order.desc("reviewDate"));
            }
        } else if ("projectId".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("projectId"));
            } else {
                criteria.addOrder(Order.desc("projectId"));
            }
        } else {
            if (asc) {
                criteria.addOrder(Order.asc("creationTime"));
            } else {
                criteria.addOrder(Order.desc("creationTime"));
            }
        }
    }

	@Override
	public Long getTotalNumberOfVaults(String userId) {
		Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0L;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        Long totalNumberOfVaults = (Long) criteria.uniqueResult();
        session.close();
        return totalNumberOfVaults;
	}

	/**
	 * Retrieve Total NUmber of rows after applying the filter
	 */
	@Override
	public Long getTotalNumberOfVaults(String userId, String query) {
		Session session = this.sessionFactory.openSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0L;
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("name", "%" + query + "%"), Restrictions.ilike("description", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        Long totalNumberOfVaults = (Long) criteria.uniqueResult();
        session.close();
        return totalNumberOfVaults;
	}

	@Override
	public List<Object[]> getAllProjectsSize() {
		Session session = this.sessionFactory.openSession();
		Query query = session.createQuery("select v.projectId, sum(v.vaultSize) from Vault v group by v.projectId");
		return (List<Object[]>)query.list();
	}

    private SchoolPermissionCriteriaBuilder createVaultCriteriaBuilder(String userId, Session session, Permission permission) {
	    return new SchoolPermissionCriteriaBuilder()
                .setCriteriaType(Vault.class)
                .setCriteriaName("vault")
                .setSession(session)
                .setTypeToSchoolAliasGenerator(criteria -> criteria.createAlias("vault.group", "group"))
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(session, userId, permission));
    }
}
