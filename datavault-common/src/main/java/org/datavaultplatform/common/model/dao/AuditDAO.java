package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Audit;

import java.util.List;

public interface AuditDAO {
    void save(Audit audit);

    void update(Audit audit);

    List<Audit> list();

    Audit findById(String Id);

    int count();

    int inProgressCount();

    List<Audit> inProgress();

    int queueCount();
}
