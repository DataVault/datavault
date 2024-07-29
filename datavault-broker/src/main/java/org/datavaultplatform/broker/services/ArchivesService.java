package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Archive;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.ArchiveDAO;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ArchivesService {

    private final ArchiveDAO archiveDAO;

    @Autowired
    public ArchivesService(ArchiveDAO archiveDAO) {
        this.archiveDAO = archiveDAO;
    }

    public List<Archive> getArchives() {
        return archiveDAO.list();
    }

    public Archive getArchive(String archiveId) {
        return archiveDAO.findById(archiveId).orElse(null);
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
    public void saveArchive(Archive archive) {
        archiveDAO.save(archive);
    }
    public Archive findById(String id) {
        return archiveDAO.findById(id).orElse(null);
    }

    public void saveOrUpdateArchive(Deposit deposit, ArchiveStore archiveStore, String archiveId) {
        String depositId = deposit == null ? null : deposit.getID();
        String archiveStoreId = archiveStore == null ? null : archiveStore.getID();
        Optional<Archive> optArchive = archiveDAO.findLatestByDepositIdAndArchiveStoreId(depositId, archiveStoreId);
        if (optArchive.isPresent()) {
            Archive archive = optArchive.get();
            archive.setArchiveId(archiveId);
            archiveDAO.save(archive);
        } else {
            addArchive(deposit, archiveStore, archiveId);
        }
    }
}
