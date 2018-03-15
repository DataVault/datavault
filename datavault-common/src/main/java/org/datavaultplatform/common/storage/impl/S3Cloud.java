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
	
	//private final AWSCredentials credentials;
    //private final TransferManager transferManager;
	private AmazonS3 s3;
    private static final String bucketName = "datavault-test-bucket";
	
	public S3Cloud(String name, Map<String, String> config) {
		super(name, config);
		// TODO Auto-generated constructor stub
		//this.credentials = new ProfileCredentialsProvider().getCredentials();
		//this.transferManager = new TransferManager(credentials);
		// the credentials are in ~/.aws/credentials
		s3 = new AmazonS3Client();
	    Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
	    s3.setRegion(euWest1);
	    
	    /*
         * List the buckets in your account
         */
        //logger.info("Listing buckets");
        //for (Bucket bucket : s3.listBuckets()) {
        //    logger.info(" - " + bucket.getName());
        //}
	}

	@Override
    public Verify.Method getVerifyMethod() {
        return this.verificationMethod;
    }

	@Override
	public long getUsableSpace() throws Exception {
		throw new UnsupportedOperationException();
	}

//	@Override
//	public void retrieve(String path, File working, Progress progress) throws Exception {
//		 throw new UnsupportedOperationException();
//	}
	
	@Override
	public void retrieve(String depositId, File working, Progress progress) throws Exception {
		//ProgressListener listener = initProgressListener(progress, true);
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

//	@Override
//    public String store(String path, File working, Progress progress) throws Exception {
//    		throw new UnsupportedOperationException();
//    }
	
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
	
	// todo: this is in the Amazon Glacier plugin too move to BaseAmazonFileSystem
//	private ProgressListener initProgressListener(final Progress progress, final boolean trackResponse) {
//
//        ProgressListener listener = new ProgressListener() {
//
//            boolean transferStarted = false;
//            boolean httpRequestInProgress = false;
//            long requestByteCount = 0;
//            long responseByteCount = 0;
//
//            @Override
//            public void progressChanged(ProgressEvent pe) {
//
//                // See https://github.com/aws/aws-sdk-java/blob/master/aws-java-sdk-core/src/main/java/com/amazonaws/event/ProgressTracker.java
//                
//                if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_STARTED_EVENT) {
//
//                    // The network transfer has started
//                    System.out.println("\tAmazon Glacier: HTTP_REQUEST_STARTED_EVENT");
//                    httpRequestInProgress = true;
//                    
//                    // If this was the first request then reset the timer (data is now flowing)
//                    if (!transferStarted) {
//                        progress.startTime = System.currentTimeMillis();
//                        progress.timestamp = System.currentTimeMillis();
//                        transferStarted = true;
//                    }
//                    
//                } else if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_COMPLETED_EVENT) {
//                    
//                    // The current network request has stopped
//                    System.out.println("\tAmazon Glacier: HTTP_REQUEST_COMPLETED_EVENT");
//                    httpRequestInProgress = false;
//                    
//                } else if (pe.getEventType() == ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT) {
//                    requestByteCount += pe.getBytesTransferred();
//                } else if (pe.getEventType() == ProgressEventType.HTTP_REQUEST_CONTENT_RESET_EVENT) {
//                    requestByteCount += (0 - pe.getBytes());
//                    
//                } else if (pe.getEventType() == ProgressEventType.RESPONSE_BYTE_TRANSFER_EVENT) {
//                    responseByteCount += pe.getBytesTransferred();
//                } else if (pe.getEventType() == ProgressEventType.HTTP_RESPONSE_CONTENT_RESET_EVENT ||
//                           pe.getEventType() == ProgressEventType.RESPONSE_BYTE_DISCARD_EVENT) {
//                    responseByteCount += (0 - pe.getBytes());
//                }
//
//                if (trackResponse) {
//                    progress.byteCount = responseByteCount;
//                    progress.timestamp = System.currentTimeMillis();
//                } else if (httpRequestInProgress) {
//                    progress.byteCount = requestByteCount;
//                    progress.timestamp = System.currentTimeMillis();
//                }
//                
//                /*
//                System.out.println("Event: " + pe.getEventType());
//                System.out.println("Byte Count: " + pe.getEventType().isByteCountEvent());
//                System.out.println("Event Bytes Transferred: " + pe.getBytesTransferred());
//                System.out.println("Event Bytes: " + pe.getBytes());
//                System.out.println("Request Bytes So Far: " + requestByteCount);
//                System.out.println("Response Bytes So Far: " + responseByteCount);
//                System.out.println("Transferred Bytes: " + progress.byteCount);
//                System.out.println("");
//                */
//            }
//        };
//        
//        return listener;
//    }

}
