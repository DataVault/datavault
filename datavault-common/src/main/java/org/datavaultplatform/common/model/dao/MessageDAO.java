package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageDAO extends JpaRepository<Message,String> {

}
