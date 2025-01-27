package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.Retrieve.Status;
import org.datavaultplatform.common.model.dao.SchoolPermissionQueryHelper;
import org.datavaultplatform.common.util.DaoUtils;

@Slf4j
public class RetrieveCustomDAOImpl extends BaseCustomDAOImpl implements RetrieveCustomDAO {

    public RetrieveCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<Retrieve> list(String query, String userId, String sort, String sortDirection, int offset, int maxResult) {
        SchoolPermissionQueryHelper<Retrieve> helper = createRetrieveQueryHelper(userId, Permission.CAN_VIEW_RETRIEVES);
        if (helper.hasNoAccess()) {
            return new ArrayList<>();
        }

        log.info("DAO.list query="+query);
        if((query == null || query.isEmpty()) == false) {
            log.info("apply restrictions");

            String queryLower = getQueryLower(query);

            helper.setSinglePredicateHelper((cb, rt) ->
                    cb.or(
                            cb.like(cb.lower(rt.get(Retrieve_.ID)), queryLower)));
        }

        if (maxResult <= 0) {
            maxResult = 10;
        }

        // Sort by creation time by default

        helper.setOrderByHelper((cb, rt) -> {
            final Path<?> sortPath;
            if (StringUtils.isBlank(sort)) {
                sortPath = rt.get(Retrieve_.timestamp);
            } else {
                sortPath = getPathFromNestedProperties(rt, sort);
            }

            Order order;
            if ("asc".equals(sortDirection)) {
                order = cb.asc(sortPath);
            } else {
                order = cb.desc(sortPath);
            }
            return List.of(order);
        });
        helper.setMaxResults(maxResult);
        helper.setFirstResult(offset);

        List<Retrieve> retrieves = helper.getItems();
        return retrieves;
        //helper.setOrderByHelper((cb,rt) -> List.of(cb.asc(rt.get(Retrieve_.TIMESTAMP))));
        //List<Retrieve> retrieves = helper.getItems();
        //return retrieves;
    }

    @Override
    public int count(String userId, String query) {
        SchoolPermissionQueryHelper<Retrieve> helper = createRetrieveQueryHelper(userId, Permission.CAN_VIEW_RETRIEVES);
        if (helper.hasNoAccess()) {
            return 0;
        }

        if((query == null || query.isEmpty()) == false) {
            log.info("apply restrictions");
            String queryLower = getQueryLower(query);
            helper.setSinglePredicateHelper((cb, rt) -> cb.or(
                    cb.like(cb.lower(rt.get(Retrieve_.ID)), queryLower)));
        }

        return helper.getItemCount().intValue();
    }

    @Override
    public int queueCount(String userId) {
        SchoolPermissionQueryHelper<Retrieve> helper = createRetrieveQueryHelper(userId, Permission.CAN_VIEW_QUEUES);
        if (helper.hasNoAccess()) {
            return 0;
        }
        helper.setSinglePredicateHelper((cb, rt) ->
            cb.equal(rt.get(Retrieve_.STATUS), Status.NOT_STARTED));
        return helper.getItemCount().intValue();
    }

    @Override
    public int inProgressCount(String userId) {
        SchoolPermissionQueryHelper<Retrieve> helper = createRetrieveQueryHelper(userId, Permission.CAN_VIEW_IN_PROGRESS);
        if (helper.hasNoAccess()) {
            return 0;
        }
        helper.setSinglePredicateHelper((cb, rt) ->
            cb.and (
                cb.notEqual(rt.get(Retrieve_.STATUS), Status.NOT_STARTED),
                cb.notEqual(rt.get(Retrieve_.STATUS), Status.COMPLETE)
            ));
        return helper.getItemCount().intValue();
    }

    @Override
    public List<Retrieve> inProgress() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Retrieve> cq = cb.createQuery(Retrieve.class);
        Root<Retrieve> root = cq.from(Retrieve.class);

        cq.where(cb.and(
            cb.notEqual(root.get(Retrieve_.STATUS), Status.NOT_STARTED),
            cb.notEqual(root.get(Retrieve_.STATUS), Status.COMPLETE)
        ));

        cq.orderBy(cb.asc(root.get(Retrieve_.TIMESTAMP)));

        List<Retrieve> retrieves = getResults(cq);
        return retrieves;
    }

    private SchoolPermissionQueryHelper<Retrieve> createRetrieveQueryHelper(String userId, Permission permission) {
        return new SchoolPermissionQueryHelper<>(em, Retrieve.class)
                .setTypeToSchoolGenerator( rt ->
                        rt.join(Retrieve_.DEPOSIT)
                        .join(Deposit_.VAULT)
                        .join(Vault_.GROUP))
                .setSchoolIds(DaoUtils.getPermittedSchoolIds(em, userId, permission));
    }
}
