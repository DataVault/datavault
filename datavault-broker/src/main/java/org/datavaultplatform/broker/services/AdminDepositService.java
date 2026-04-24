package org.datavaultplatform.broker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@ConditionalOnBean(Sender.class)
public class AdminDepositService {
    
    private static final Logger LOG = LoggerFactory.getLogger(AdminDepositService.class);
    
    private final ArchiveStoreService archiveStoreService;
    private final JobsService jobsService;
    private final Sender sender;
    private final boolean workersSendDeletedChunkEvents;

    public AdminDepositService(ArchiveStoreService archiveStoreService,
                               JobsService jobsService, Sender sender,
                               @Value("${workers.send.deleted.chunk.events:false}") boolean workersSendDeletedChunkEvents) {
        this.archiveStoreService = archiveStoreService;
        this.jobsService = jobsService;
        this.sender = sender;
        this.workersSendDeletedChunkEvents = workersSendDeletedChunkEvents;
    }

    public void deleteDeposit(Deposit deposit, User user) throws Exception {
        final String userId = user == null ? null : user.getID();
        LOG.info("Delete deposit with name [{}] userId[{}]", deposit.getName(), userId);

        List<Job> jobs = deposit.getJobs();
        for (Job job : jobs) {
            if (job.isError() == false && job.getState() != job.getStates().size() - 1) {
                // There's an in-progress job for this deposit
                throw new IllegalArgumentException("Job in-progress for this Deposit");
            }
        }

        List<ArchiveStore> archiveStores = archiveStoreService.getArchiveStores();
        if (archiveStores.isEmpty()) {
            throw new Exception("No configured archive storage");
        }

        LOG.info("Delete deposit archiveStores : {}", archiveStores);
        archiveStores = archiveStoreService.addArchiveSpecificOptions(archiveStores);

        // Create a job to track this delete
        Job job = new Job("org.datavaultplatform.worker.tasks.Delete");
        jobsService.addJob(deposit, job);

        // Ask the worker to process the data delete
        try {

            HashMap<String, String> deleteProperties = new HashMap<>();
            deleteProperties.put(PropNames.DEPOSIT_ID, deposit.getID());
            deleteProperties.put(PropNames.BAG_ID, deposit.getBagId());
            deleteProperties.put(PropNames.ARCHIVE_SIZE, Long.toString(deposit.getArchiveSize()));
            // NOTE : for scheduled deleted = the userId will be null
            deleteProperties.put(PropNames.USER_ID, userId);
            deleteProperties.put(PropNames.NUM_OF_CHUNKS, Integer.toString(deposit.getNumOfChunks()));
            for (Archive archive : deposit.getArchives()) {
                deleteProperties.put(archive.getArchiveStore().getID(), archive.getArchiveId());
            }
            deleteProperties.put(PropNames.WORKERS_SEND_DELETED_CHUNK_EVENTS,
                    Boolean.toString(workersSendDeletedChunkEvents));

            // Add a single entry for the user file storage
            Map<String, String> userFileStoreClasses = new HashMap<>();
            Map<String, Map<String, String>> userFileStoreProperties = new HashMap<>();
            //userFileStoreClasses.put(storageID, userStore.getStorageClass());
            //userFileStoreProperties.put(storageID, userStore.getProperties());

            Task deleteTask = new Task(
                    job, deleteProperties, archiveStores,
                    userFileStoreProperties, userFileStoreClasses,
                    null, null,
                    null,
                    null, null,
                    null, null, null);
            ObjectMapper mapper = new ObjectMapper();
            String jsonDelete = mapper.writeValueAsString(deleteTask);
            sender.send(jsonDelete);
        } catch (Exception e) {
            LOG.error("Exception while deleting a deposit", e);
        }
    }
}
