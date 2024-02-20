package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.datavaultplatform.common.model.BillingInfo;
import org.datavaultplatform.common.model.BillingInfo_;

public class BillingCustomDAOImpl extends BaseCustomDAOImpl implements BillingCustomDAO {

  public BillingCustomDAOImpl(EntityManager em) {
    super(em);
  }

    @Override
    public List<BillingInfo> list(String sort, String order, String offset, String maxResult) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BillingInfo> cr = cb.createQuery(BillingInfo.class).distinct(true);
        Root<BillingInfo> rt = cr.from(BillingInfo.class);

        if ("asc".equals(order)) {
            cr.orderBy(cb.asc(rt.get(sort)));
        } else {
            cr.orderBy(cb.desc(rt.get(sort)));
        }

        TypedQuery<BillingInfo> typedQuery = restrict(cr, offset, maxResult);

        List<BillingInfo> vaults = getBillingInfo(typedQuery);
        return vaults;
    }

    @Override
    public List<BillingInfo> search(String query, String sort, String order, String offset,  String maxResult) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BillingInfo> cr = cb.createQuery(BillingInfo.class).distinct(true);
        Root<BillingInfo> rt = cr.from(BillingInfo.class);

        if(query != null){
          String queryLower = getQueryLower(query);
          cr.where(cb.or(
              cb.like(cb.lower(rt.get(BillingInfo_.id)), queryLower),
              cb.like(cb.lower(rt.get(BillingInfo_.contactName)), queryLower)
          ));
        }

        if ("desc".equals(order)) {
            cr.orderBy(cb.desc(rt.get(sort)));
        } else {
            cr.orderBy(cb.asc(rt.get(sort)));
        }

        TypedQuery<BillingInfo> typedQuery = restrict(cr, offset, maxResult);

        List<BillingInfo> vaults = getBillingInfo(typedQuery);
        return vaults;
    }


    private TypedQuery<BillingInfo> restrict(CriteriaQuery<BillingInfo> cr, String offset, String maxResult){
      TypedQuery<BillingInfo> typedQuery = em.createQuery(cr);

      if ((offset != null && maxResult != null) && !maxResult.equals("0")) {
        typedQuery.setMaxResults(Integer.parseInt(maxResult));
        typedQuery.setFirstResult(Integer.parseInt(offset));
      }
      return typedQuery;
    }

  /**
   * Retrieve Total NUmber of rows after applying the filter
   */
  @Override
  public Long countByQuery(String query) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class).distinct(true);
    Root<BillingInfo> rt = countQuery.from(BillingInfo.class);

    countQuery.select(cb.count(rt));
    if (query != null) {
      String queryLower = getQueryLower(query);
      countQuery.where(cb.or(
          cb.like(cb.lower(rt.get(BillingInfo_.id)), queryLower),
          cb.like(cb.lower(rt.get(BillingInfo_.contactName)), queryLower)
      ));
    }

    Long count = getSingleResult(countQuery);
    return count;
  }


  List<BillingInfo> getBillingInfo(TypedQuery<BillingInfo> typedQuery) {
    return getResults(BillingInfo.class, typedQuery);
  }
}
