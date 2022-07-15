package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Deposit.Status;
import org.datavaultplatform.common.model.Deposit_;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.User_;
import org.datavaultplatform.common.model.Vault_;
import org.datavaultplatform.common.model.dao.SchoolPermissionQueryHelper;
import org.datavaultplatform.common.util.DaoUtils;

@Slf4j
public class DepositCustomDAOImpl extends BaseCustomDAOImpl implements DepositCustomDAO {

  public DepositCustomDAOImpl(EntityManager em) {
    super(em);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Deposit> list(String query, String userId, String sort, String sortDirection, int offset, int maxResult) {
    log.info("query:"+query+", sort: "+sort+", order: "+sortDirection +", offset: "+offset+", maxResult: "+maxResult);
    SchoolPermissionQueryHelper<Deposit> helper = createDepositQueryHelper(userId, Permission.CAN_MANAGE_DEPOSITS);
    if (helper.hasNoAccess()) {
      return new ArrayList<>();
    }

    log.info("DAO.list query="+query);
    if((query == null || "".equals(query)) == false) {
      log.info("apply restrictions");

      String queryLower = getQueryLower(query);

      helper.setSinglePredicateHelper((cb, rt) ->
          cb.or(
              cb.like(cb.lower(rt.get(Deposit_.ID)), queryLower),
              cb.like(cb.lower(rt.get(Deposit_.NAME)), queryLower)));
    }

    if (maxResult <= 0) {
      maxResult = 10;
    }

    // Sort by creation time by default

    helper.setOrderByHelper((cb, rt) -> {
      final Path sortPath;
      if (StringUtils.isBlank(sort)) {
        sortPath = rt.get(Deposit_.creationTime);
      } else if ("userID".equals(sort)) {
        sortPath = rt.get(Deposit_.user).get(User_.id);
      } else if ("vaultID".equals(sort)) {
        sortPath = rt.get(Deposit_.vault).get(Vault_.id);
      } else {
        sortPath = rt.get(sort);
      }

      Order order;
      if ("asc".equals(sortDirection)) {
        order = cb.asc(sortPath);
      } else {
        order = cb.desc(sortPath);
      }
      return Collections.singletonList(order);
    });
    helper.setMaxResults(maxResult);
    helper.setFirstResult(offset);

    List<Deposit> deposits = helper.getItems();
    return deposits;
  }

  @Override
  public int count(String userId, String query) {
    SchoolPermissionQueryHelper<Deposit> helper = createDepositQueryHelper(userId, Permission.CAN_MANAGE_DEPOSITS);
    if (helper.hasNoAccess()) {
      return 0;
    }

    if((query == null || "".equals(query)) == false) {
      log.info("apply restrictions");
      String queryLower = getQueryLower(query);
      helper.setSinglePredicateHelper((cb, rt) -> cb.or(
          cb.like(cb.lower(rt.get(Deposit_.ID)), queryLower),
          cb.like(cb.lower(rt.get(Deposit_.NAME)), queryLower)));
    }
    return helper.getItemCount().intValue();
  }

  @Override
  public int queueCount(String userId) {
    SchoolPermissionQueryHelper<Deposit> helper = createDepositQueryHelper(userId, Permission.CAN_VIEW_QUEUES);
    if (helper.hasNoAccess()) {
      return 0;
    }
    helper.setSinglePredicateHelper((cb, rt) ->
        cb.equal(rt.get(Deposit_.STATUS), Status.NOT_STARTED));
    return helper.getItemCount().intValue();
  }

  @Override
  public int inProgressCount(String userId) {
    SchoolPermissionQueryHelper<Deposit> helper = createDepositQueryHelper(userId, Permission.CAN_VIEW_IN_PROGRESS);
    if (helper.hasNoAccess()) {
      return 0;
    }
    helper.setSinglePredicateHelper((cb, rt) -> cb.and (
        cb.notEqual(rt.get(Deposit_.STATUS), Status.NOT_STARTED),
        cb.notEqual(rt.get(Deposit_.STATUS), Status.COMPLETE)));
    return helper.getItemCount().intValue();
  }

  @Override
  public List<Deposit> inProgress() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Deposit> cq = cb.createQuery(Deposit.class).distinct(true);
    Root<Deposit> root = cq.from(Deposit.class);
    cq.where(cb.and(
        cb.notEqual(root.get(Deposit_.STATUS), Status.NOT_STARTED),
        cb.notEqual(root.get(Deposit_.STATUS), Status.COMPLETE)));
    List<Deposit> deposits = getResults(cq);
    return deposits;
  }

  @Override
  public List<Deposit> completed() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Deposit> cq = cb.createQuery(Deposit.class).distinct(true);
    Root<Deposit> root = cq.from(Deposit.class);
    cq.where(cb.equal(root.get(Deposit_.STATUS), Status.COMPLETE));
    List<Deposit> deposits = getResults(cq);
    return deposits;
  }

  @Override
  public List<Deposit> search(String query, String sort, String sortDirection, String userId) {
    SchoolPermissionQueryHelper<Deposit> helper = createDepositQueryHelper(userId, Permission.CAN_MANAGE_DEPOSITS);
    if (helper.hasNoAccess()) {
      return new ArrayList<>();
    }

    String queryLower = getQueryLower(query);

    helper.setSinglePredicateHelper((cb, rt) ->
        cb.or(
            cb.like(cb.lower(rt.get(Deposit_.ID)), queryLower),
            cb.like(cb.lower(rt.get(Deposit_.NAME)), queryLower),
            cb.like(cb.lower(rt.get(Deposit_.FILE_PATH)), queryLower)
        )
    );

    helper.setOrderByHelper((cb, rt) -> {
      Path sortPath = rt.get(sort);
      final Order order;
      if("asc".equals(sortDirection)) {
        order = cb.asc(sortPath);
      } else {
        order = cb.desc(sortPath);
      }
      return Collections.singletonList(order);
    });

    List<Deposit> deposits = helper.getItems();
    return deposits;
  }

  @Override
  public Long size(String userId) {
    SchoolPermissionQueryHelper<Deposit> helper = createDepositQueryHelper(userId,  Permission.CAN_VIEW_VAULTS_SIZE);
    if (helper.hasNoAccess()) {
      return 0L;
    }
    return helper.queryForNumber((cb, rt) -> cb.sum(rt.get(Deposit_.depositSize)));
  }

  private SchoolPermissionQueryHelper<Deposit> createDepositQueryHelper(String userId,
      Permission permission) {
    return new SchoolPermissionQueryHelper<>(em, Deposit.class)
        .setTypeToSchoolGenerator(root -> root.join(Deposit_.vault).join(Vault_.group))
        .setSchoolIds(DaoUtils.getPermittedSchoolIds(em, userId, permission));
  }

  @Override
  public List<Deposit> getDepositsWaitingForAudit(Date olderThanDate) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Deposit> cq = cb.createQuery(Deposit.class).distinct(true);
    Root<Deposit> root = cq.from(Deposit.class);
    cq.where(cb.and(
        cb.lessThanOrEqualTo(root.get(Deposit_.CREATION_TIME), olderThanDate),
        cb.lessThanOrEqualTo(root.get(Deposit_.STATUS), Deposit.Status.COMPLETE)
    ));
    cq.orderBy(cb.asc(root.get(Deposit_.CREATION_TIME)));

    List<Deposit> deposits = getResults(cq);
    return deposits;
  }
}
