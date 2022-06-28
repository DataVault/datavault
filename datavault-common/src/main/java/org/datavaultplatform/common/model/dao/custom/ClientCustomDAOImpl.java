package org.datavaultplatform.common.model.dao.custom;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.datavaultplatform.common.model.Client;
import org.datavaultplatform.common.model.Client_;

public class ClientCustomDAOImpl extends BaseCustomDAOImpl implements ClientCustomDAO {

    public ClientCustomDAOImpl(EntityManager em) {
        super(em);
    }

    @Override
    public Client findByApiKey(String apiKey) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Client> cr = cb.createQuery(Client.class);
        Root<Client> rt = cr.from(Client.class);
        cr.where(cb.equal(rt.get(Client_.apiKey), apiKey));
        return getSingleResult(cr);
    }

}
