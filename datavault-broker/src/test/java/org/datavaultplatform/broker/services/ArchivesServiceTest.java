package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Archive;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.dao.ArchiveDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchivesServiceTest {

    @Mock
    ArchiveDAO mArchiveDao;

    ArchivesService service;

    @BeforeEach
    void setup() {
        service = new ArchivesService(mArchiveDao);
    }

    @Nested
    class saveOrUpdateArchiveTests {

        @Captor
        ArgumentCaptor<Archive> argArchive;

        Archive archive;
        Deposit deposit;
        ArchiveStore archiveStore;

        @BeforeEach
        void setup() {
            deposit = new Deposit() {
                @Override
                public String getID() {
                    return "depositId";
                }
            };
            archiveStore = new ArchiveStore() {
                @Override
                public String getID() {
                    return "archiveStoreId";
                }
            };
            archive = new Archive() {
                @Override
                public String getId() {
                    return "archiveId";
                }
            };
        }

        @Test
        void testExists() {

            when(mArchiveDao.findLatestByDepositIdAndArchiveStoreId("depositId", "archiveStoreId")).thenReturn(Optional.of(archive));

            when(mArchiveDao.save(argArchive.capture())).thenReturn(archive);

            //we are testing this method
            service.saveOrUpdateArchive(deposit, archiveStore, "archiveIdUpdated");

            assertThat(archive.getArchiveId()).isEqualTo("archiveIdUpdated");

            verify(mArchiveDao).save(argArchive.getValue());

            verify(mArchiveDao).findLatestByDepositIdAndArchiveStoreId("depositId", "archiveStoreId");

            verifyNoMoreInteractions(mArchiveDao);
        }

        @Test
        void testDoesNotExist() {

            when(mArchiveDao.save(argArchive.capture())).thenReturn(null);

            when(mArchiveDao.findLatestByDepositIdAndArchiveStoreId("depositId", "archiveStoreId")).thenReturn(Optional.empty());

            //we are testing this method
            service.saveOrUpdateArchive(deposit, archiveStore, "archiveId");

            verify(mArchiveDao).findLatestByDepositIdAndArchiveStoreId("depositId", "archiveStoreId");

            Archive archiveSaved = argArchive.getValue();
            verify(mArchiveDao).save(archiveSaved);

            assertThat(archiveSaved.getArchiveId()).isEqualTo("archiveId");
            assertThat(archiveSaved.getArchiveStore()).isEqualTo(archiveStore);
            assertThat(archiveSaved.getDeposit()).isEqualTo(deposit);

            verifyNoMoreInteractions(mArchiveDao);
        }
    }
}