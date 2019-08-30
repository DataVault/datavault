package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.RoleUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;

public class RoleAssignmentDaoImpl implements RoleAssignmentDAO {

    private SessionFactory sessionFactory;

    public RoleAssignmentDaoImpl() {
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void synchroniseDataOwners() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();

            Criteria dataOwnerRoleCriteria = session.createCriteria(RoleModel.class);
            dataOwnerRoleCriteria.add(Restrictions.eq("name", RoleUtils.DATA_OWNER_ROLE_NAME));
            RoleModel dataOwnerRole = (RoleModel) dataOwnerRoleCriteria.uniqueResult();

            Criteria vaultsCriteria = session.createCriteria(Vault.class);
            List<Vault> allVaults = vaultsCriteria.list();

            Criteria existingDataOwnerCriteria = session.createCriteria(RoleAssignment.class, "roleAssignment");
            existingDataOwnerCriteria.add(Restrictions.eq("role", dataOwnerRole));
            List<RoleAssignment> existingDataOwners = existingDataOwnerCriteria.list();

            for (Vault vault : allVaults) {
                Optional<RoleAssignment> vaultOwner = existingDataOwners.stream()
                        .filter(roleAssignment -> vault.equals(roleAssignment.getVault()))
                        .findFirst();

                if (vaultOwner.isPresent() && !vaultOwner.get().getUser().equals(vault.getUser())) {
                    // The Vaults idea of its owner doesn't match the RoleAssignments idea of the owner
                    // => Update the Vault to match the RoleAssignment
                    vault.setUser(vaultOwner.get().getUser());
                    session.update(vault);

                } else if (!vaultOwner.isPresent() && vault.getUser() != null) {
                    // The vault is not orphaned but there is no RoleAssignment for the vault owner
                    // => Create new RoleAssignment
                    RoleAssignment dataOwner = new RoleAssignment();
                    dataOwner.setUser(vault.getUser());
                    dataOwner.setVault(vault);
                    dataOwner.setRole(dataOwnerRole);
                    session.save(dataOwner);
                }
            }

            transaction.commit();

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean roleAssignmentExists(RoleAssignment roleAssignment) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Criteria criteria = session.createCriteria(RoleAssignment.class);
            criteria.add(Restrictions.eq("role", roleAssignment.getRole()));
            criteria.add(Restrictions.eq("user", roleAssignment.getUser()));
            if (roleAssignment.getSchool() != null) {
                criteria.add(Restrictions.eq("school", roleAssignment.getSchool()));
            }
            if (roleAssignment.getVault() != null) {
                criteria.add(Restrictions.eq("vault", roleAssignment.getVault()));
            }
            return criteria.uniqueResult() != null;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public void store(RoleAssignment roleAssignment) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.persist(roleAssignment);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public RoleAssignment find(Long id) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Criteria criteria = session.createCriteria(RoleAssignment.class);
            criteria.add(Restrictions.eq("id", id));
            RoleAssignment roleAssignment = (RoleAssignment) criteria.uniqueResult();
            return roleAssignment;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findAll() {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Criteria criteria = session.createCriteria(RoleAssignment.class);
            List<RoleAssignment> roleAssignments = criteria.list();
            return roleAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findBySchoolId(String schoolId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            Group school = findObjectById(session, Group.class, "id", schoolId);
            List<RoleAssignment> schoolAssignments = findBy(session, "school", school);
            return schoolAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    private <T> T findObjectById(Session session, Class<T> type, String idName, Object idValue) {
        Criteria criteria = session.createCriteria(type);
        criteria.add(Restrictions.eq(idName, idValue));
        return (T) criteria.uniqueResult();
    }

    private List<RoleAssignment> findBy(Session session, String columnName, Object columnValue) {
        Criteria criteria = session.createCriteria(RoleAssignment.class);
        criteria.add(Restrictions.eq(columnName, columnValue));
        return (List<RoleAssignment>) criteria.list();
    }

    @Override
    public List<RoleAssignment> findByVaultId(String vaultId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            Vault vault = findObjectById(session, Vault.class, "id", vaultId);
            List<RoleAssignment> schoolAssignments = findBy(session, "vault", vault);
            return schoolAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findByUserId(String userId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            User user = findObjectById(session, User.class, "id", userId);
            List<RoleAssignment> schoolAssignments = findBy(session, "user", user);
            return schoolAssignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public List<RoleAssignment> findByRoleId(Long roleId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            RoleModel role = findObjectById(session, RoleModel.class, "id", roleId);
            List<RoleAssignment> assignments = findBy(session, "role", role);
            return assignments;
        } finally {
            if (session != null) session.close();
        }

    }

    @Override
    public void update(RoleAssignment roleAssignment) {
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.update(roleAssignment);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public void delete(Long id) {
        RoleAssignment roleAssignment = find(id);
        Session session = null;
        try {
            session = this.sessionFactory.openSession();

            Transaction tx = session.beginTransaction();
            session.delete(roleAssignment);
            tx.commit();
        } finally {
            if (session != null) session.close();
        }
    }
}
