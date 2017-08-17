package oracle.cloudstorage.ftm.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.CloudStorageClass;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.TransferResult;
import oracle.cloudstorage.ftm.UploadConfig;
import oracle.cloudstorage.ftm.exception.ClientException;

public class UploadFileDemo {
	private static final String className = UploadFileDemo.class.getSimpleName();
	private static final String demoAccountPropertiesFilepath = "my-account.properties";
	private static final Logger logger = LoggerFactory.getLogger(className);

	public static void main(String[] args) throws Exception {
		SetupSamplesLog.initLogging(className + ".log");
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(demoAccountPropertiesFilepath)) {
			prop.load(is);
		} catch (Exception e) {
			logger.info("Failed to read demo account properties file.");
			throw e;
		}
		FileTransferAuth auth = new FileTransferAuth(prop.getProperty("user-name"),
				prop.getProperty("password").toCharArray(),
				prop.getProperty("service-name"), prop.getProperty("service-url"), prop.getProperty("identity-domain"));

		FileTransferManager manager = null;
		try {
			manager = FileTransferManager.getDefaultFileTransferManager(auth);

			String containerName = "ftm-demo-archive";
			File file = new File("/tmp/foo");
			UploadConfig uploadConfig = new UploadConfig();
			uploadConfig.setOverwrite(true);
			uploadConfig.setStorageClass(CloudStorageClass.Archive);
			uploadConfig.setSegmentSize(1024 * 1024);
			logger.info("Uploading file " + file.getName() + " to container " + containerName);
			TransferResult uploadResult = manager.upload(uploadConfig, containerName, null, file);
			logger.info("Upload completed. Result:" + uploadResult.toString());
		} catch (ClientException ce) {
			System.out.println("Operation failed. " + ce.getMessage());
		} finally {
			if (manager != null) {
				manager.shutdown();
			}
		}

	}
}
