package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.PendingVault_;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.User_;
import org.datavaultplatform.common.model.dao.SchoolPermissionQueryHelper;
import org.datavaultplatform.common.util.DaoUtils;

public class PendingVaultCustomDAOImpl extends BaseCustomDAOImpl implements
    PendingVaultCustomDAO {

  public PendingVaultCustomDAOImpl(EntityManager em) {
    super(em);
  }


  @Override
  public List<PendingVault> list(String userId, String sort, String order, String offset, String maxResult) {
    SchoolPermissionQueryHelper<PendingVault> helper = createPendingVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
    if (helper.hasNoAccess()) {
      return new ArrayList<>();
    }
    order(sort, order, helper);

    if((offset != null && maxResult != null) && !maxResult.equals("0")) {
      helper.setMaxResults(Integer.valueOf(maxResult));
      helper.setFirstResult(Integer.valueOf(offset));
    }

    List<PendingVault> vaults = helper.getItems();
    return vaults;
  }

  @Override
  public int getTotalNumberOfPendingVaults(String userId, String confirmed) {
    SchoolPermissionQueryHelper<PendingVault> helper = createPendingVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
    if (helper.hasNoAccess()) {
      return 0;
    }
    helper.setPredicateHelper((cb, rt) -> {
      List<Predicate> predicates = new ArrayList<>();
      addConfirmed(predicates, confirmed, cb, rt);
      return predicates;
    });
    int totalNumberOfVaults = helper.getItemCount().intValue();
    return totalNumberOfVaults;
  }

  /**
   * Retrieve Total NUmber of rows after applying the filter
   */
  @Override
  public int getTotalNumberOfPendingVaults(String userId, String query, String confirmed) {
    SchoolPermissionQueryHelper<PendingVault> helper = createPendingVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
    if (helper.hasNoAccess()) {
      return 0;
    }
    addCommonPredicates(helper, query, confirmed);
    int totalNumberOfVaults = helper.getItemCount().intValue();
    return totalNumberOfVaults;
  }

  @Override
  public List<PendingVault> search(String userId, String query, String sort, String order, String offset, String maxResult, String confirmed) {
    SchoolPermissionQueryHelper<PendingVault> helper = createPendingVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
    if (helper.hasNoAccess()) {
      return new ArrayList<>();
    }
    addCommonPredicates(helper, query, confirmed);

    order(sort, order, helper);
    if((offset != null && maxResult != null) && !maxResult.equals("0")) {
      helper.setMaxResults(Integer.valueOf(maxResult));
      helper.setFirstResult(Integer.valueOf(offset));
    }

    List<PendingVault> vaults = helper.getItems();
    return vaults;
  }

  @Override
  public int count(String userId) {
    SchoolPermissionQueryHelper<PendingVault> helper = createPendingVaultQueryHelper(userId, Permission.CAN_MANAGE_VAULTS);
    if (helper.hasNoAccess()) {
      return 0;
    }
    return helper.getItemCount().intValue();
  }

  private SchoolPermissionQueryHelper<PendingVault> createPendingVaultQueryHelper(String userId,
      Permission permission) {
    return new SchoolPermissionQueryHelper<>(em, PendingVault.class)
        .setTypeToSchoolGenerator(rt -> rt.join(PendingVault_.group))
        .setSchoolIds(DaoUtils.getPermittedSchoolIds(em, userId, permission));
  }

  private void order(String sort, String direction,
      SchoolPermissionQueryHelper<PendingVault> helper) {
    // Default to ascending order
    boolean asc = "desc".equals(direction) ? false : true;

//        // See if there is a valid sort option

    helper.setOrderByHelper((cb, rt) -> {
      Path<?> sortPath;
      if ("user".equals(sort)) {
        sortPath = rt.get(PendingVault_.user).get(User_.id);
      } else {
        sortPath = rt.get(sort);
      }
      return getSingletonOrderList(cb, asc, sortPath);
    });
  }

  void addCommonPredicates(SchoolPermissionQueryHelper<PendingVault> helper, String query,
      String confirmed) {
    helper.setPredicateHelper((cb, rt) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (!(query == null || query.equals(""))) {
        String lowerQuery = getQueryLower(query);
        predicates.add(
            cb.or(
                cb.like(cb.lower(rt.get(PendingVault_.ID)), lowerQuery),
                cb.like(cb.lower(rt.get(PendingVault_.NAME)), lowerQuery),
                cb.like(cb.lower(rt.get(PendingVault_.DESCRIPTION)), lowerQuery)
            )
        );
      }

      addConfirmed(predicates, confirmed, cb, rt);
      return predicates;
    });
  }

  private void addConfirmed(List<Predicate> predicates, String confirmed, CriteriaBuilder cb,
      Root<PendingVault> rt) {
    if (confirmed != null && !confirmed.equals("null") && !confirmed.equals("")) {
      Boolean conf = Boolean.valueOf(confirmed);
      predicates.add(
          cb.equal(rt.get(PendingVault_.CONFIRMED), conf)
      );
    }
  }
  private static List<Order> getSingletonOrderList(CriteriaBuilder cb, boolean asc, Path<?> sortPath) {
    return Collections.singletonList(asc ? cb.asc(sortPath) : cb.desc(sortPath));
  }
}
