package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.dao.SchoolPermissionCriteriaBuilder;
import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class PendingVaultCustomDAOImpl extends BaseCustomDAOImpl implements
    PendingVaultCustomDAO {

    public PendingVaultCustomDAOImpl(EntityManager em) {
        super(em);
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<PendingVault> list(String userId, String sort, String order, String offset, String maxResult) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createPendingVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return new ArrayList<>();
        }
        Criteria criteria = criteriaBuilder.build();
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        order(sort, order, criteria);
        
        if((offset != null && maxResult != null) && !maxResult.equals("0")) {
        	criteria.setMaxResults(Integer.valueOf(maxResult));
        	criteria.setFirstResult(Integer.valueOf(offset));
        }

        List<PendingVault> vaults = criteria.list();
        return vaults;
    }
    
    @Override
	public int getTotalNumberOfPendingVaults(String userId, String confirmed) {
		Session session = this.getCurrentSession();
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
        return totalNumberOfVaults;
	}

	/**
	 * Retrieve Total NUmber of rows after applying the filter
	 */
	@Override
	public int getTotalNumberOfPendingVaults(String userId, String query, String confirmed) {
		Session session = this.getCurrentSession();
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
        return totalNumberOfVaults;
	}
    
    @SuppressWarnings("unchecked")
    @Override
    public List<PendingVault> search(String userId, String query, String sort, String order, String offset, String maxResult, String confirmed) {
        Session session = this.getCurrentSession();
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
        if((offset != null && maxResult != null) && !maxResult.equals("0")) {
        	criteria.setMaxResults(Integer.valueOf(maxResult));
        	criteria.setFirstResult(Integer.valueOf(offset));
        }

        List<PendingVault> vaults = criteria.list();
        return vaults;
    }

    @Override
    public int count(String userId) {
        Session session = this.getCurrentSession();
        SchoolPermissionCriteriaBuilder criteriaBuilder = createPendingVaultCriteriaBuilder(userId, session, Permission.CAN_MANAGE_VAULTS);
        if (criteriaBuilder.hasNoAccess()) {
            return 0;
        }
        Criteria criteria = criteriaBuilder.build();
        Long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return count.intValue();
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
}
