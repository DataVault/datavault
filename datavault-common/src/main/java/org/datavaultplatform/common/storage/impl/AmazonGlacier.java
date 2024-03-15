package org.datavaultplatform.common.storage.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

// Documentation:
// http://docs.aws.amazon.com/amazonglacier/latest/dev/using-aws-sdk-for-java.html
// http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/glacier/AmazonGlacierClient.html

@Slf4j
public class AmazonGlacier extends Device implements ArchiveStore {
    
    // Amazon Glacier has retrieval costs so a full copy-back is undesirable.
    // TODO: verify the hashes which are available from a glacier vault inventory.
    // Vault inventory is carried out every 24 hours.
    public final Verify.Method verificationMethod = Verify.Method.LOCAL_ONLY;
    
    // A reference to the account which is related to the current credentials
    private final String DEFAULT_ACCOUNT_NAME = "-";
    
    private final String glacierVault;
    private final String awsRegion;
    private final String accessKey;
    private final String secretKey;
    
    private final AWSCredentials credentials;
    private final AmazonGlacierClient glacierClient;
    private final AmazonSQSClient sqsClient;
    private final AmazonSNSClient snsClient;
    private final ArchiveTransferManager transferManager;
    
    public AmazonGlacier(String name, Map<String,String> config) {
        super(name, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        glacierVault = config.get("glacierVault"); // e.g. datavault-test
        awsRegion = config.get("awsRegion"); // e.g. eu-west-1.amazonaws.com
        accessKey = config.get("accessKey");
        secretKey = config.get("secretKey");
        
        // Connect
        log.info("Connecting to " + awsRegion);
        
        credentials = new BasicAWSCredentials(accessKey, secretKey);

        sqsClient = new AmazonSQSClient(credentials);
        sqsClient.setEndpoint("sqs." + awsRegion);
        
        snsClient = new AmazonSNSClient(credentials);
        snsClient.setEndpoint("sns." + awsRegion);
        
        glacierClient = new AmazonGlacierClient(credentials);
        glacierClient.setEndpoint("glacier." + awsRegion);
        
        transferManager = new ArchiveTransferManager(glacierClient, sqsClient, snsClient);
        
        // Verify parameters are correct.
        /*
        logger.info("Available glacier vaults:");
        ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
        ListVaultsResult listVaultsResult = glacierClient.listVaults(listVaultsRequest);
        for (DescribeVaultOutput vault : listVaultsResult.getVaultList()) {
            logger.info(vault.getVaultName());
        }
        */
    }
    
 // todo: this is in the Amazon Glacier plugin too move to BaseAmazonFileSystem
    private ProgressListener initProgressListener(final Progress progress, final boolean trackResponse) {

        ProgressListener listener = new ProgressListener() {

            boolean transferStarted = false;
            boolean httpRequestInProgress = false;
            long requestByteCount = 0;
            long responseByteCount = 0;

            @Override
            public void progressChanged(ProgressEvent pe) {

                // See https://github.com/aws/aws-sdk-java/blob/master/aws-java-sdk-core/src/main/java/com/amazonaws/event/ProgressTracker.java
                
                if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_STARTED_EVENT) {

                    // The network transfer has started
                    log.info("\tAmazon Glacier: HTTP_REQUEST_STARTED_EVENT");
                    httpRequestInProgress = true;
                    
                    // If this was the first request then reset the timer (data is now flowing)
                    if (!transferStarted) {
                        long now = System.currentTimeMillis();
                        progress.setStartTime(now);
                        progress.setTimestamp(now);
                        transferStarted = true;
                    }
                    
                } else if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_COMPLETED_EVENT) {
                    
                    // The current network request has stopped
                    log.info("\tAmazon Glacier: HTTP_REQUEST_COMPLETED_EVENT");
                    httpRequestInProgress = false;
                    
                } else if (pe.getEventType() == ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT) {
                    requestByteCount += pe.getBytesTransferred();
                } else if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_CONTENT_RESET_EVENT) {
                    requestByteCount += (0 - pe.getBytes());
                    
                } else if (pe.getEventType() == ProgressEventType.RESPONSE_BYTE_TRANSFER_EVENT) {
                    responseByteCount += pe.getBytesTransferred();
                } else if (pe.getEventType() == ProgressEventType.HTTP_RESPONSE_CONTENT_RESET_EVENT ||
                           pe.getEventType() == ProgressEventType.RESPONSE_BYTE_DISCARD_EVENT) {
                    responseByteCount += (0 - pe.getBytes());
                }

                if (trackResponse) {
                    progress.setByteCount(responseByteCount);
                    progress.setTimestamp(System.currentTimeMillis());
                } else if (httpRequestInProgress) {
                    progress.setByteCount(requestByteCount);
                    progress.setTimestamp(System.currentTimeMillis());
                }
                /*
                logger.info("Event: " + pe.getEventType());
                logger.info("Byte Count: " + pe.getEventType().isByteCountEvent());
                logger.info("Event Bytes Transferred: " + pe.getBytesTransferred());
                logger.info("Event Bytes: " + pe.getBytes());
                logger.info("Request Bytes So Far: " + requestByteCount);
                logger.info("Response Bytes So Far: " + responseByteCount);
                logger.info("Transferred Bytes: " + progress.byteCount);
                logger.info("");
                */
            }
        };
        
        return listener;
    }
    
    @Override
    public long getUsableSpace() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public void retrieve(String path, File working, Progress progress) {
        
        ProgressListener listener = initProgressListener(progress, true);
        
        transferManager.download(DEFAULT_ACCOUNT_NAME, glacierVault, path, working, listener);
    }

    @Override
    public String store(String path, File working, Progress progress) {
        
        ProgressListener listener = initProgressListener(progress, false);
        
        // Note: this is using the passed path as the deposit description.
        // We should probably use the deposit UUID instead (do we need a specialised archive method)?
        String archiveId = transferManager.upload(DEFAULT_ACCOUNT_NAME, glacierVault, path, working, listener).getArchiveId();
        
        // Glacier generates a new ID which is required retrieve data.
        return archiveId;
    }

    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }

    @Override
    public Logger getLogger() {
        return log;
    }
}
