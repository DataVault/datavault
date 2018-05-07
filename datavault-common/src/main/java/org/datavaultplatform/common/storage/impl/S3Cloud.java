package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Cloud extends Device implements ArchiveStore {

	private static final Logger logger = LoggerFactory.getLogger(S3Cloud.class);
	private static String defaultBucketName = "datavault-test-bucket";
	private static String defaultRegionName = Regions.EU_WEST_1.toString();
	public Verify.Method verificationMethod = Verify.Method.CLOUD;
	private AmazonS3 s3;
	private String bucketName;

	public S3Cloud(String name, Map<String, String> config) {
		super(name, config);
		super.depositIdStorageKey = true;
		String bucketName = config.get("s3.bucketName");
		if ( bucketName == null ) {
			bucketName = defaultBucketName;
		}
		String regionName = config.get("s3.region");
		if ( regionName == null ) {
			regionName = defaultRegionName;
		}
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder
			                        .standard()
			                        .withRegion(regionName);
                // Access key can be provided through properties, but if not will be picked up default location, e.g. ~/.aws/credentials
		String accessKey = config.get("s3.awsAccessKey");
		String secretKey = config.get("s3.awsSecretKey");
                if (accessKey != null && secretKey != null) {
                	builder = builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
                }
		this.s3 = builder.build();
		this.bucketName = bucketName;
	}

	@Override
    public Verify.Method getVerifyMethod() {
        return this.verificationMethod;
    }

	@Override
	public long getUsableSpace() throws Exception {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void retrieve(String depositId, File working, Progress progress) throws Exception {
		logger.info("Downloading an object");
		try {
			S3Object object = s3.getObject(new GetObjectRequest(bucketName, depositId));
			logger.info("Content-Type: "  + object.getObjectMetadata().getContentType());
			S3ObjectInputStream s3is = object.getObjectContent();
			FileOutputStream fos = new FileOutputStream(working);
			byte[] read_buf = new byte[1024];
		    int read_len = 0;
		    while ((read_len = s3is.read(read_buf)) > 0) {
		        fos.write(read_buf, 0, read_len);
		    }
		    s3is.close();
		    fos.close();
			
		} catch (AmazonServiceException ase) {
			logger.info("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            logger.info("Error Message:    " + ase.getMessage());
            logger.info("HTTP Status Code: " + ase.getStatusCode());
            logger.info("AWS Error Code:   " + ase.getErrorCode());
            logger.info("Error Type:       " + ase.getErrorType());
            logger.info("Request ID:       " + ase.getRequestId());
            throw ase;
		} catch (FileNotFoundException fe) {
			logger.info(fe.getMessage());
                        throw fe;
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
			logger.info("Error Message: " + ace.getMessage());
                        throw ace;
		}
	}
	
	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {
		
		//ProgressListener listener = initProgressListener(progress, true);
		logger.info("Uploading " + working.getName() + " to " + this.bucketName);
		try {
			s3.putObject(new PutObjectRequest(bucketName, depositId, working));
		} catch (AmazonServiceException ase) {
			logger.info("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            logger.info("Error Message:    " + ase.getMessage());
            logger.info("HTTP Status Code: " + ase.getStatusCode());
            logger.info("AWS Error Code:   " + ase.getErrorCode());
            logger.info("Error Type:       " + ase.getErrorType());
            logger.info("Request ID:       " + ase.getRequestId());
            throw ase;
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
			logger.info("Error Message: " + ace.getMessage());
                        throw ace;
		}
		return depositId;
	}

}
