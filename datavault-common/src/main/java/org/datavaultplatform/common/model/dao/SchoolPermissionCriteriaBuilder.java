package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.util.DaoUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.Set;
import java.util.function.Function;

public class SchoolPermissionCriteriaBuilder {

    private Session session;

    private Class<?> criteriaType;

    private String criteriaName;

    private Set<String> schoolIds;

    private Function<Criteria, Criteria> typeToSchoolAliasGenerator;

    public SchoolPermissionCriteriaBuilder() {
        typeToSchoolAliasGenerator = c -> c;
    }

    public SchoolPermissionCriteriaBuilder setSession(Session session) {
        this.session = session;
        return this;
    }

    public SchoolPermissionCriteriaBuilder setCriteriaType(Class<?> criteriaType) {
        this.criteriaType = criteriaType;
        return this;
    }

    public SchoolPermissionCriteriaBuilder setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
        return this;
    }

    public SchoolPermissionCriteriaBuilder setSchoolIds(Set<String> schoolIds) {
        this.schoolIds = schoolIds;
        return this;
    }

    public SchoolPermissionCriteriaBuilder setTypeToSchoolAliasGenerator(Function<Criteria, Criteria> typeToSchoolAliasGenerator) {
        this.typeToSchoolAliasGenerator = typeToSchoolAliasGenerator;
        return this;
    }

    public boolean hasNoAccess() {
        return schoolIds == null || schoolIds.isEmpty();
    }

    public Criteria build() {
        if (hasNoAccess()) {
            throw new IllegalStateException("Cannot build school permissions criteria without any permitted schools");
        }
        Criteria criteria = session.createCriteria(criteriaType, criteriaName);
        if (!schoolIds.contains(DaoUtils.FULL_ACCESS_INDICATOR)) {
            criteria = typeToSchoolAliasGenerator.apply(criteria);
            criteria.add(Restrictions.in("group.id", schoolIds));
        }
        return criteria;
    }
}
