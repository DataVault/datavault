package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.dao.ArchiveStoreDAO;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
@Service
public class ArchiveStoreService {

    private String optionsDir;
    private String tempDir;
    private String bucketName;
    private String region;
    private String awsAccessKey;
    private String awsSecretKey;
    private String tsmRetryTime;
    private String occRetryTime;
    private String tsmMaxRetries;
    private String occMaxRetries;
    private String ociNameSpace;
    private String ociBucketName;

    private ArchiveStoreDAO archiveStoreDAO;

    public void setOptionsDir(String optionsDir) {
        this.optionsDir = optionsDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public void setTsmRetryTime(String tsmRetryTime) {
        this.tsmRetryTime = tsmRetryTime;
    }

    public void setOccRetryTime(String occRetryTime) {
        this.occRetryTime = occRetryTime;
    }

    public void setTsmMaxRetries(String tsmMaxRetries) {
        this.tsmMaxRetries = tsmMaxRetries;
    }

    public void setOccMaxRetries(String occMaxRetries) {
        this.occMaxRetries = occMaxRetries;
    }

    public void setOciNameSpace(String ociNameSpace) { this.ociNameSpace = ociNameSpace; }

    public void setOciBucketName(String ociBucketName) { this.ociBucketName = ociBucketName; }

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
        return archiveStoreDAO.findById(archiveStoreID);
    }

    public ArchiveStore getForRetrieval() {
        return archiveStoreDAO.findForRetrieval();
    }

    public void deleteArchiveStore(String archiveStoreID) {
        archiveStoreDAO.deleteById(archiveStoreID);
    }

    public void setArchiveStoreDAO(ArchiveStoreDAO archiveStoreDAO) {
        this.archiveStoreDAO = archiveStoreDAO;
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

