package org.datavaultplatform.common.storage.impl;

import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.io.Progress;

import java.util.Map;
import java.io.File;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.amazonaws.services.glacier.model.*;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

// Documentation:
// http://docs.aws.amazon.com/amazonglacier/latest/dev/using-aws-sdk-for-java.html
// http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/glacier/AmazonGlacierClient.html

public class AmazonGlacier extends Device implements ArchiveStore {
    
    // A reference to the account which is related to the current credentials
    private String DEFAULT_ACCOUNT_NAME = "-";
    
    private String glacierVault;
    private String awsRegion;
    private String accessKey;
    private String secretKey;
    
    private final AWSCredentials credentials;
    private final AmazonGlacierClient glacierClient;
    private final AmazonSQSClient sqsClient;
    private final AmazonSNSClient snsClient;
    private final ArchiveTransferManager transferManager;
    
    public AmazonGlacier(String name, Map<String,String> config) throws Exception  {
        super(name, config);
        
        // Unpack the config parameters (in an implementation-specific way)
        glacierVault = config.get("glacierVault"); // e.g. datavault-test
        awsRegion = config.get("awsRegion"); // e.g. eu-west-1.amazonaws.com
        accessKey = config.get("accessKey");
        secretKey = config.get("secretKey");
        
        // Connect
        System.out.println("Connecting to " + awsRegion);
        
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
        System.out.println("Available glacier vaults:");
        ListVaultsRequest listVaultsRequest = new ListVaultsRequest();
        ListVaultsResult listVaultsResult = glacierClient.listVaults(listVaultsRequest);
        for (DescribeVaultOutput vault : listVaultsResult.getVaultList()) {
            System.out.println(vault.getVaultName());
        }
        */
    }
    
    private ProgressListener initProgressListener(final Progress progress) {

        ProgressListener listener = new ProgressListener() {

            final long TIMESTAMP_INTERVAL = 100; // ms

            boolean httpRequestStarted = false;
            long streamByteCount = 0;

            @Override
            public void progressChanged(ProgressEvent pe) {

                if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_STARTED_EVENT) {

                    // The network transfer has started
                    System.out.println("\tAmazon Glacier: HTTP_REQUEST_STARTED_EVENT");
                    httpRequestStarted = true;

                    // Reset the timer
                    progress.startTime = System.currentTimeMillis();
                    progress.timestamp = System.currentTimeMillis();
                }

                if (pe.getEventType() == ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT) {
                    streamByteCount += pe.getBytesTransferred();
                } else if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_CONTENT_RESET_EVENT) {
                    // This will be a negative quantity!
                    streamByteCount += pe.getBytesTransferred();
                }

                if (httpRequestStarted) {
                    long timestamp = System.currentTimeMillis();
                    if (timestamp > (progress.timestamp + TIMESTAMP_INTERVAL)) {
                        progress.byteCount = streamByteCount;
                        progress.timestamp = timestamp;
                    }
                }

                System.out.println("Event: " + pe.getEventType());
                System.out.println("Byte Count: " + pe.getEventType().isByteCountEvent());
                System.out.println("Event Bytes Transferred: " + pe.getBytesTransferred());
                System.out.println("Event Bytes: " + pe.getBytes());
                System.out.println("Stream Bytes So Far: " + streamByteCount);
                System.out.println("Transferred Bytes: " + progress.byteCount);
                System.out.println("");
            }
        };
        
        return listener;
    }
    
    @Override
    public long getUsableSpace() throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
        
        ProgressListener listener = initProgressListener(progress);
        
        transferManager.download(DEFAULT_ACCOUNT_NAME, glacierVault, path, working, listener);
    }

    @Override
    public String store(String path, File working, Progress progress) throws Exception {
        
        ProgressListener listener = initProgressListener(progress);
        
        // Note: this is using the passed path as the deposit description.
        // We should probably use the deposit UUID instead (do we need a specialised archive method)?
        String archiveId = transferManager.upload(DEFAULT_ACCOUNT_NAME, glacierVault, path, working, listener).getArchiveId();
        
        // Glacier generates a new ID which is required retrieve data.
        return archiveId;
    }
}
