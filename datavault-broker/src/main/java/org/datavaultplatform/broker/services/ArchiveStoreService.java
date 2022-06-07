package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.ArchiveStoreDAO;

import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
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
            @Value("${s3.bucketName:#{null}}") String ociBucketName,
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

                if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.TivoliStorageManager")) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.optionsDir != null && ! this.optionsDir.equals("")) {
                        asProps.put("optionsDir", this.optionsDir);
                    }
                    if (this.tempDir != null && ! this.tempDir.equals("")) {
                        asProps.put("tempDir", this.tempDir);
                    }
                    if (this.tsmRetryTime != null && ! this.tsmRetryTime.equals("")) {
                        asProps.put("tsmRetryTime", this.tsmRetryTime);
                    }
                    if (this.tsmMaxRetries != null && ! this.tsmMaxRetries.equals("")) {
                        asProps.put("tsmMaxRetries", this.tsmMaxRetries);
                    }
                    archiveStore.setProperties(asProps);
                }

                if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic")) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.occRetryTime != null && ! this.occRetryTime.equals("")) {
                        asProps.put("occRetryTime", this.occRetryTime);
                    }
                    if (this.occMaxRetries != null && ! this.occMaxRetries.equals("")) {
                        asProps.put("occMaxRetries", this.occMaxRetries);
                    }
                    if (this.ociBucketName != null && ! this.ociBucketName.equals("")) {
                        asProps.put("ociBucketName", this.ociBucketName);
                    }
                    if (this.ociNameSpace != null && ! this.ociNameSpace.equals("")) {
                        asProps.put("ociNameSpace", this.ociNameSpace);
                    }
                    archiveStore.setProperties(asProps);
                }

                if (archiveStore.getStorageClass().equals("org.datavaultplatform.common.storage.impl.S3Cloud")) {
                    HashMap<String, String> asProps = archiveStore.getProperties();
                    if (this.bucketName != null && ! this.bucketName.equals("")) {
                        asProps.put("s3.bucketName", this.bucketName);
                    }
                    if (this.region != null && ! this.region.equals("")) {
                        asProps.put("s3.region", this.region);
                    }
                    if (this.awsAccessKey != null && ! this.awsAccessKey.equals("")) {
                        asProps.put("s3.awsAccessKey", this.awsAccessKey);
                    }
                    if (this.awsSecretKey != null && ! this.awsSecretKey.equals("")) {
                        asProps.put("s3.awsSecretKey", this.awsSecretKey);
                    }

                    archiveStore.setProperties(asProps);
                }
            }
        }

        return archiveStores;
    }
}

