package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Archive;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.ArchiveDAO;

import java.util.Date;
import java.util.List;


public class ArchivesService {

    private ArchiveDAO archiveDAO;

    public void setArchiveDAO(ArchiveDAO archiveDAO) {
        this.archiveDAO = archiveDAO;
    }

    public List<Archive> getArchives() {
        return archiveDAO.list();
    }

    public void addArchive(Archive archive,
                           Deposit deposit,
                           ArchiveStore archiveStore) {

        Date d = new Date();
        archive.setCreationTime(d);

        archive.setDeposit(deposit);
        archive.setArchiveStore(archiveStore);

        archiveDAO.save(archive);
    }

    public void updateArchive(Archive archive) {
        archiveDAO.update(archive);
    }

}
