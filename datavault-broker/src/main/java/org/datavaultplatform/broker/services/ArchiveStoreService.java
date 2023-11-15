package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.ArchiveStoreDAO;

import java.util.HashMap;
import java.util.List;
import org.datavaultplatform.common.storage.StorageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArchiveStoreService {

    private final String optionsDir;
    private final String tempDir;
    private final String bucketName;
    private final String region;
    private final String awsAccessKey;
    private final String awsSecretKey;
    private final String tsmRetryTime;
    private final String occRetryTime;
    private final String tsmMaxRetries;
    private final String occMaxRetries;
    private final String ociNameSpace;
    private final String ociBucketName;

    private final String tsmReverse;

    private final ArchiveStoreDAO archiveStoreDAO;

    @Autowired
    public ArchiveStoreService
        (   @Value("${optionsDir:#{null}}")String optionsDir,
            @Value("${tempDir:#{null}}") String tempDir,
            @Value("${s3.bucketName:#{null}}") String bucketName,
            @Value("${s3.region:#{null}}") String region,
            @Value("${s3.awsAccessKey:#{null}}") String awsAccessKey,
            @Value("${s3.awsSecretKey:#{null}}") String awsSecretKey,
            @Value("${tsmRetryTime:#{null}}") String tsmRetryTime,
            @Value("${occRetryTime:#{null}}") String occRetryTime,
            @Value("${tsmMaxRetries:#{null}}") String tsmMaxRetries,
            @Value("${occMaxRetries:#{null}}") String occMaxRetries,
            @Value("${ociNameSpace:#{null}}") String ociNameSpace,
            @Value("${ociBucketName:#{null}}") String ociBucketName,
            @Value("${tsmReverse:#{false}}") String tsmReverse,
            ArchiveStoreDAO archiveStoreDAO) {
        this.optionsDir = optionsDir;
        this.tempDir = tempDir;
        this.bucketName = bucketName;
        this.region = region;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.tsmRetryTime = tsmRetryTime;
        this.occRetryTime = occRetryTime;
        this.tsmMaxRetries = tsmMaxRetries;
        this.occMaxRetries = occMaxRetries;
        this.ociNameSpace = ociNameSpace;
        this.ociBucketName = ociBucketName;
        this.tsmReverse = tsmReverse;
        this.archiveStoreDAO = archiveStoreDAO;
    }


    public List<ArchiveStore> getArchiveStores() {
        return archiveStoreDAO.list();
    }

    public void addArchiveStore(ArchiveStore archiveStore) {

        archiveStoreDAO.save(archiveStore);
    }

    public void updateArchiveStore(ArchiveStore archiveStore) {
        archiveStoreDAO.update(archiveStore);
    }

    public ArchiveStore getArchiveStore(String archiveStoreID) {
        return archiveStoreDAO.findById(archiveStoreID).orElse(null);
    }

    public ArchiveStore getForRetrieval() {
        return archiveStoreDAO.findForRetrieval();
    }

    public void deleteArchiveStore(String archiveStoreID) {
        archiveStoreDAO.deleteById(archiveStoreID);
    }

    /*
    There are copies of this code elsewhere. Someone with spare time should delete them and call this method instead.
     */
    public List<ArchiveStore> addArchiveSpecificOptions(List<ArchiveStore> archiveStores) {
        if (archiveStores != null && ! archiveStores.isEmpty()) {
            for (ArchiveStore archiveStore : archiveStores) {

                if (archiveStore.isTivoliStorageManager()) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.optionsDir != null && ! this.optionsDir.equals("")) {
                        asProps.put(PropNames.OPTIONS_DIR, this.optionsDir);
                    }
                    if (this.tempDir != null && ! this.tempDir.equals("")) {
                        asProps.put(PropNames.TEMP_DIR, this.tempDir);
                    }
                    if (this.tsmRetryTime != null && ! this.tsmRetryTime.equals("")) {
                        asProps.put(PropNames.TSM_RETRY_TIME, this.tsmRetryTime);
                    }
                    if (this.tsmMaxRetries != null && ! this.tsmMaxRetries.equals("")) {
                        asProps.put(PropNames.TSM_MAX_RETRIES, this.tsmMaxRetries);
                    }
                    if (this.tsmReverse != null && ! this.tsmReverse.equals("")) {
                        asProps.put(PropNames.TSM_REVERSE, this.tsmReverse);
                    }
                    archiveStore.setProperties(asProps);
                }

                if (archiveStore.isOracle()) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.occRetryTime != null && ! this.occRetryTime.equals("")) {
                        asProps.put(PropNames.OCC_RETRY_TIME, this.occRetryTime);
                    }
                    if (this.occMaxRetries != null && ! this.occMaxRetries.equals("")) {
                        asProps.put(PropNames.OCC_MAX_RETRIES, this.occMaxRetries);
                    }
                    if (this.ociBucketName != null && ! this.ociBucketName.equals("")) {
                        asProps.put(PropNames.OCI_BUCKET_NAME, this.ociBucketName);
                    }
                    if (this.ociNameSpace != null && ! this.ociNameSpace.equals("")) {
                        asProps.put(PropNames.OCI_NAME_SPACE, this.ociNameSpace);
                    }
                    archiveStore.setProperties(asProps);
                }

                if (archiveStore.isAmazonS3()) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.bucketName != null && ! this.bucketName.equals("")) {
                        asProps.put(PropNames.AWS_S3_BUCKET_NAME, this.bucketName);
                    }
                    if (this.region != null && ! this.region.equals("")) {
                        asProps.put(PropNames.AWS_S3_REGION, this.region);
                    }
                    if (this.awsAccessKey != null && ! this.awsAccessKey.equals("")) {
                        asProps.put(PropNames.AWS_ACCESS_KEY, this.awsAccessKey);
                    }
                    if (this.awsSecretKey != null && ! this.awsSecretKey.equals("")) {
                        asProps.put(PropNames.AWS_SECRET_KEY, this.awsSecretKey);
                    }

                    archiveStore.setProperties(asProps);
                }
            }
        }

        return archiveStores;
    }
}

