package org.datavaultplatform.common.model.dao.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.model.Deposit_;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Retrieve.Status;
import org.datavaultplatform.common.model.Retrieve_;
import org.datavaultplatform.common.model.Vault_;
import org.datavaultplatform.common.model.dao.SchoolPermissionQueryHelper;
import org.datavaultplatform.common.util.DaoUtils;


public class RetrieveCustomDAOImpl extends BaseCustomDAOImpl implements RetrieveCustomDAO {

    public RetrieveCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public List<Retrieve> list(String userId) {
        SchoolPermissionQueryHelper<Retrieve> helper = createRetrieveQueryHelper(userId, Permission.CAN_VIEW_RETRIEVES);
        if (helper.hasNoAccess()) {
            return new ArrayList<>();
        }
        helper.setOrderByHelper((cb,rt) -> Collections.singletonList(cb.asc(rt.get(Retrieve_.TIMESTAMP))));
        List<Retrieve> retrieves = helper.getItems();
        return retrieves;
    }

    @Override
    public int count(String userId) {
        SchoolPermissionQueryHelper<Retrieve> helper = createRetrieveQueryHelper(userId, Permission.CAN_VIEW_RETRIEVES);
        if (helper.hasNoAccess()) {
            return 0;
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
