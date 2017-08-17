package oracle.cloudstorage.ftm.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.CloudStorageClass;
import oracle.cloudstorage.ftm.DownloadConfig;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.TransferResult;
import oracle.cloudstorage.ftm.UploadConfig;
import oracle.cloudstorage.ftm.exception.ClientException;
import oracle.cloudstorage.ftm.model.ObjectRestoreJob;

public class RestoreObjectDemo {
	private static final String className = RestoreObjectDemo.class.getSimpleName();
	private static final String demoAccountPropertiesFilepath = "my-account.properties";
	private static final Logger logger = LoggerFactory.getLogger(className);

	private static final String demoContainerPrefix = "ftmapi-demo-arch-";
	private String archContainerName = demoContainerPrefix + System.currentTimeMillis();;
	private String archSegmentsContainerName = archContainerName + "_segments";
	private FileTransferManager manager = null;
	private File file = null;
	private static final String restoreDirPath = "restoredfiles";

	public static void main(String[] args) throws Exception {
		SetupSamplesLog.initLogging(className + ".log");
		RestoreObjectDemo demo = new RestoreObjectDemo();
		demo.demo();
	}

	public void demo() throws Exception {
		Properties prop = new Properties();
		try (InputStream is = new FileInputStream(demoAccountPropertiesFilepath)) {
			prop.load(is);
		} catch (Exception e) {
			logger.info("Failed to read demo account properties file.");
			throw e;
		}
		FileTransferAuth auth = new FileTransferAuth(prop.getProperty("user-name"),
				prop.getProperty("password").toCharArray(), prop.getProperty("service-name"),
				prop.getProperty("service-url"), prop.getProperty("identity-domain"));
		try {
			UploadConfig uploadConfig = new UploadConfig();
			uploadConfig.setStorageClass(CloudStorageClass.Archive);
			// archContainerName = demoContainerPrefix +
			// System.currentTimeMillis();
			manager = FileTransferManager.getDefaultFileTransferManager(auth);

			// Create a temp small file and upload it to archive container
			file = createTempFile();
			String demoObject = file.getName();
			manager.upload(uploadConfig, archContainerName, demoObject, file);
			// Restore the object
			logger.info("Initiating restore of " + demoObject);
			ObjectRestoreJob orj = manager.restoreObject(archContainerName, demoObject);
			logger.info("Restore job status after initiating restore: " + orj.toString());


			// Create a temp large file (SLO) and upload it to archive container
			file = createTempLargeFile();
			String largeDemoObject = file.getName() + "_SLO";

			uploadConfig.setSegmentSize(1024 * 1024); // Segment size = 1 MiB
			uploadConfig.setSegmentsContainer(archSegmentsContainerName);
			manager.upload(uploadConfig, archContainerName, largeDemoObject, file);
			// Restore the object
			logger.info("Initiating restore of " + largeDemoObject);
			orj = manager.restoreObject(archContainerName, largeDemoObject);
			logger.info("Restore job status after initiating restore: " + orj.toString());

			// Wait for restore job to complete for the file.
			waitForRestoreJobToFinish(archContainerName, demoObject);
			// Download the object
			File destFile = new File(restoreDirPath + File.separator + demoObject);
			DownloadConfig downloadConfig = new DownloadConfig();
			downloadConfig.setDownloadRestoredObjectOnly(true);
			TransferResult transferResult = manager.download(downloadConfig, archContainerName, demoObject, destFile);
			logger.info(transferResult.toString());

			// Wait for restore job to complete for the large file.
			waitForRestoreJobToFinish(archContainerName, largeDemoObject);
			// Download the large object
			destFile = new File(restoreDirPath + File.separator + largeDemoObject);
			transferResult = manager.download(downloadConfig, archContainerName, largeDemoObject, destFile);
			logger.info(transferResult.toString());
		} catch (ClientException ce) {
			System.out.println("Operation failed. " + ce.getMessage());
			ce.printStackTrace();
		} finally {
			if (manager != null) {
				manager.shutdown();
			}
		}
	}

	private void waitForRestoreJobToFinish(String containerName, String objectName) throws InterruptedException {
		ObjectRestoreJob orj = manager.getObjectRestoreJob(containerName, objectName);
		logger.info(orj.toString());
		while (!orj.isCompleted()) {
			Thread.sleep(5 * 60 * 1000);
			orj = manager.getObjectRestoreJob(containerName, objectName);
			logger.info(orj.toString());
		}
	}
	private static File createTempFile() throws IOException {
		File file = File.createTempFile("ftmapi-demo-arch-", ".txt", null);
		file.deleteOnExit();
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.setLength(10 * 1024);
		}
		return file;
	}

	private static File createTempLargeFile() throws IOException {
		File file = File.createTempFile("ftmapi-demo-arch-", ".txt", null);
		file.deleteOnExit();
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
			raf.setLength(2 * 1024 * 1024);
		}
		return file;
	}
}
