package org.datavaultplatform.common.model.dao;


import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseDAO<T> extends AbstractDAO<T,String> {
}

