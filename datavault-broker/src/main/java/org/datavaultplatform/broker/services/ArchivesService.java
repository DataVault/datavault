package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Archive;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.ArchiveDAO;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class ArchivesService {

    private ArchiveDAO archiveDAO;

    public void setArchiveDAO(ArchiveDAO archiveDAO) {
        this.archiveDAO = archiveDAO;
    }

    public List<Archive> getArchives() {
        return archiveDAO.list();
    }

    public Archive getArchive(String archiveId) {
        return archiveDAO.findById(archiveId);
    }

    public void addArchive(Deposit deposit, ArchiveStore archiveStore, String archiveId) {

        Archive archive = new Archive();

        Date d = new Date();
        archive.setCreationTime(d);

        archive.setDeposit(deposit);
        archive.setArchiveStore(archiveStore);
        archive.setArchiveId(archiveId);

        archiveDAO.save(archive);
    }

    public void updateArchive(Archive archive) {
        archiveDAO.update(archive);
    }
}
