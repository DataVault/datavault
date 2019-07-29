package org.datavaultplatform.common.model.dao;

import java.util.List;

import org.datavaultplatform.common.model.BillingInfo;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillingDAOImpl implements BillingDAO {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BillingDAOImpl.class);

    private SessionFactory sessionFactory;
 
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
     
    @Override
    public void save(BillingInfo billing) {
        Session session = this.sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        session.persist(billing);
        tx.commit();
        session.close();
    }
 
    @Override
    public void update(BillingInfo billing) {        
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.update(billing);
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
    public void saveOrUpdateVault(BillingInfo billing) {        
        Session session = null;
        Transaction tx = null;
        try {
            session = this.sessionFactory.openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(billing);
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
    public List<BillingInfo> list() {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(BillingInfo.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.addOrder(Order.asc("creationTime"));
        List<BillingInfo> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BillingInfo> list(String sort, String order, String offset, String maxResult) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(BillingInfo.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        order(sort, order, criteria);
        if((offset != null && maxResult != null) || !maxResult.equals("0")) {
        	criteria.setMaxResults(Integer.valueOf(maxResult));
        	criteria.setFirstResult(Integer.valueOf(offset));
        }

        List<BillingInfo> vaults = criteria.list();
        session.close();
        return vaults;
    }
    
    @Override
    public BillingInfo findById(String Id) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(BillingInfo.class);
        criteria.add(Restrictions.eq("id", Id));
        BillingInfo vault = (BillingInfo)criteria.uniqueResult();
        session.close();
        return vault;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BillingInfo> search(String query, String sort, String order, String offset, String maxResult) {
        Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(BillingInfo.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("name", "%" + query + "%"), Restrictions.ilike("description", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        order(sort, order, criteria);
        if((offset != null && maxResult != null) || !maxResult.equals("0")) {
        	criteria.setMaxResults(Integer.valueOf(maxResult));
        	criteria.setFirstResult(Integer.valueOf(offset));
        }

        List<BillingInfo> vaults = criteria.list();
        session.close();
        return vaults;
    }

    @Override
    public int count() {
        Session session = this.sessionFactory.openSession();
        return (int)(long)(Long)session.createCriteria(BillingInfo.class).setProjection(Projections.rowCount()).uniqueResult();
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
        }
        else if ("vaultSize".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("vaultSize"));
            } else {
                criteria.addOrder(Order.desc("vaultSize"));
            }
        
        } else if ("reviewDate".equals(sort)) {
            if (asc) {
                criteria.addOrder(Order.asc("reviewDate"));
            } else {
                criteria.addOrder(Order.desc("reviewDate"));
            }
        } else {
            if (asc) {
                criteria.addOrder(Order.asc("creationTime"));
            } else {
                criteria.addOrder(Order.desc("creationTime"));
            }}
        }


	@Override
	public Long getTotalNumberOfVaults() {
		Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(BillingInfo.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        Long totalNoOfRows = (Long) criteria.uniqueResult();

        session.close();
        return totalNoOfRows;
	}
	/**
	 * Retrieve Total NUmber of rows after applying the filter
	 */
	public Long getTotalNumberOfVaults(String query) {
		Session session = this.sessionFactory.openSession();
        Criteria criteria = session.createCriteria(BillingInfo.class);
        criteria.add(Restrictions.or(Restrictions.ilike("id", "%" + query + "%"), Restrictions.ilike("name", "%" + query + "%"), Restrictions.ilike("description", "%" + query + "%")));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        Long totalNoOfRows = (Long) criteria.uniqueResult();

        session.close();
        return totalNoOfRows;
	}

}
