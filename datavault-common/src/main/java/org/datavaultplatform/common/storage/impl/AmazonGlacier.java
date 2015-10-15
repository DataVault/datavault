package org.datavaultplatform.common.storage.impl;

import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.io.Progress;

import java.util.Map;
import java.io.File;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import com.amazonaws.services.glacier.model.*;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class AmazonGlacier extends Device implements ArchiveStore {
    
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
    
    @Override
    public long getUsableSpace() throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
        transferManager.download(glacierVault, path, working);
    }

    @Override
    public String store(String path, File working, Progress progress) throws Exception {
        // Note: this is using the passed path as the deposit description.
        // We should probably use the deposit UUID instead (do we need a specialised archive method)?
        String archiveId = transferManager.upload(glacierVault, path, working).getArchiveId();

        // Glacier generates a new ID which is required retrieve data.
        return archiveId;
    }
}
