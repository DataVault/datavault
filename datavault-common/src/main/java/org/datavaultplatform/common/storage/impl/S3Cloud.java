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
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3Cloud extends Device implements ArchiveStore {

	private static final Logger logger = LoggerFactory.getLogger(S3Cloud.class);
	public Verify.Method verificationMethod = Verify.Method.COPY_BACK;
	private AmazonS3 s3;
    private static String bucketName = "datavault-test-bucket";
	
	public S3Cloud(String name, Map<String, String> config) {
		super(name, config);
		super.depositIdStorageKey = true;
		if (config.containsKey("bucketName")) {
        	String bucketName = config.get("bucketName");
        	S3Cloud.bucketName = bucketName;
        }
		// the auth credentials are in ~/.aws/credentials
		s3 = new AmazonS3Client();
	    Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
	    s3.setRegion(euWest1);
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
		} catch (FileNotFoundException fe) {
			logger.info(fe.getMessage());
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
			logger.info("Error Message: " + ace.getMessage());
		}
	}
	
	@Override
	public String store(String depositId, File working, Progress progress) throws Exception {
		
		//ProgressListener listener = initProgressListener(progress, true);
		logger.info("Uploading " + working.getName() + " to " + S3Cloud.bucketName);
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
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
			logger.info("Error Message: " + ace.getMessage());
		}
		return depositId;
	}

}
