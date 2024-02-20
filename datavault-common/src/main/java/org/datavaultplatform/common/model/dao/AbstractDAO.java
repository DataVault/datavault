package org.datavaultplatform.common.model.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface AbstractDAO<T,ID> extends JpaRepository<T,ID> {

  default List<T> list(){
    return findAll();
  }

  default T update(T item) {
    return this.save(item);
  }
}
