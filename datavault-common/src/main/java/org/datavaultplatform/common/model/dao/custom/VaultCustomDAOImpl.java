package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.model.Dataset_;
import org.datavaultplatform.common.model.Group_;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.User_;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.common.model.Vault_;
import org.datavaultplatform.common.model.dao.SchoolPermissionQueryHelper;
import org.datavaultplatform.common.util.DaoUtils;


public class VaultCustomDAOImpl extends BaseCustomDAOImpl implements VaultCustomDAO {

    public VaultCustomDAOImpl(EntityManager em) {
        super(em);
    }


    @Override
    public List<Vault> list(String userId, String sort, String order, String offset, String maxResult) {
        SchoolPermissionQueryHelper<Vault> helper = createVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
        if (helper.hasNoAccess()) {
            return new ArrayList<>();
        }

        order(sort, order, helper);
        if ((offset != null && maxResult != null) && !maxResult.equals("0")) {
            helper.setMaxResults(Integer.valueOf(maxResult));
            helper.setFirstResult(Integer.valueOf(offset));
        }

        // With mysql 5.7 - We can't mix 'order by' of non-selected columns with 'distinct' -
        List<Vault> vaults = helper.getItems(false);
        // we perform distinct on Java side
        return distinctVaults(vaults);
    }

    @Override
    public List<Vault> search(String userId, String query, String sort, String order, String offset, String maxResult) {
        SchoolPermissionQueryHelper<Vault> helper = createVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
        if (helper.hasNoAccess()) {
            return new ArrayList<>();
        }
        if (StringUtils.isNotBlank(query)) {
            String queryLower = getQueryLower(query);
            helper.setSinglePredicateHelper((cb, rt) -> cb.or (
                cb.like(cb.lower(rt.get(Vault_.ID)), queryLower),
                cb.like(cb.lower(rt.get(Vault_.NAME)), queryLower),
                cb.like(cb.lower(rt.get(Vault_.DESCRIPTION)), queryLower)
            ));
        }

        order(sort, order, helper);
        if ((offset != null && maxResult != null) && !maxResult.equals("0")) {
            helper.setMaxResults(Integer.valueOf(maxResult));
            helper.setFirstResult(Integer.valueOf(offset));
        }

        // With mysql 5.7 - We can't mix 'order by' of non-selected columns with 'distinct' -
        List<Vault> vaults = helper.getItems(false);
        // we perform distinct on Java side
        return distinctVaults(vaults);
    }

    // With mysql 5.7 - We can't mix 'order by' of non-selected columns with 'distinct' -
    // but we can perform distinct on the Java side
    protected static List<Vault> distinctVaults(List<Vault> vaults) {
        Map<String,Vault> byId = vaults.stream().collect(Collectors.toMap(
            Vault::getID,
            vt -> vt,
            (x, y) -> y,
            LinkedHashMap::new)
        );
        return new ArrayList<>(byId.values());
    }

    @Override
    public int count(String userId) {
        SchoolPermissionQueryHelper<Vault> helper = createVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
        if (helper.hasNoAccess()) {
            return 0;
        }
        return helper.getItemCount().intValue();
    }

    @Override
    public int getRetentionPolicyCount(int status) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Vault> root = cq.from(Vault.class);
        cq.select(cb.countDistinct(root));

        Predicate[] clauseArr = {
            cb.equal(root.get(Vault_.RETENTION_POLICY_STATUS), status)
        };
        cq.where(clauseArr);

        return getSingleResult(cq).intValue();
    }

    private void order(String sort, String order, SchoolPermissionQueryHelper<Vault> helper) {
        // Default to ascending order
        boolean asc = !"desc".equals(order);


        // See if there is a valid sort option
        helper.setOrderByHelper((cb,rt) -> {
            Expression<?> sortExpr;
            // See if there is a valid sort option
            if ("user".equals(sort)) {
                sortExpr = rt.get(Vault_.user).get(User_.id);
            } else if ("groupID".equals(sort)) {
                sortExpr = rt.get(Vault_.group).get(Group_.id);
            } else if ("crisID".equals(sort)) {
                sortExpr = rt.join(Vault_.dataset).get(Dataset_.crisId);
            } else {
                sortExpr = rt.get(sort);
            }
            jakarta.persistence.criteria.Order orderBy;
            if (asc) {
                orderBy = cb.asc(sortExpr);
            } else {
                orderBy= cb.desc(sortExpr);
            }
            return Collections.singletonList(orderBy);
        });
    }

	@Override
	public int getTotalNumberOfVaults(String userId) {
    SchoolPermissionQueryHelper<Vault> helper = createVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
    if (helper.hasNoAccess()) {
        return 0;
    }
    return helper.getItemCount().intValue();
	}

	/**
	 * Retrieve Total NUmber of rows after applying the filter
	 */
	@Override
	public int getTotalNumberOfVaults(String userId, String query) {
      SchoolPermissionQueryHelper<Vault> helper = createVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
      if (helper.hasNoAccess()) {
          return 0;
      }
      if (StringUtils.isNotBlank(query)) {
          String queryLower = getQueryLower(query);
          helper.setSinglePredicateHelper((cb, rt) ->
              cb.or(
                  cb.like(cb.lower(rt.get(Vault_.id)), queryLower),
                  cb.like(cb.lower(rt.get(Vault_.name)), queryLower),
                  cb.like(cb.lower(rt.get(Vault_.description)), queryLower)));
      }
      return helper.getItemCount().intValue();
  }

	@Override
	public List<Object[]> getAllProjectsSize() {
    Query query = em.createQuery(
          "select v.projectId, sum(v.vaultSize) from org.datavaultplatform.common.model.Vault v group by v.projectId");
		return query.getResultList();
	}


    private SchoolPermissionQueryHelper<Vault> createVaultQueryHelper(String userId, Permission permission) {
	    return new SchoolPermissionQueryHelper<>(em, Vault.class)
                .setTypeToSchoolGenerator(rt -> rt.join(Vault_.group))
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(em, userId, permission));
    }
}
