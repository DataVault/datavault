package oracle.cloudstorage.ftm.samples;

/**
 * Sample code for demonstrating the use FTM API for transferring files to/from Oracle archive storage service.
 * 
 * This account specified by 'my-account.properties' file must have Archive service enabled and read/write permissions
 * in order to create container and write objects.
 * 
 * The download of an archived object typically takes 2-4 hours, since the object must be restored first. So this sample code
 * may take several hours to complete the execution.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.CloudStorageClass;
import oracle.cloudstorage.ftm.DownloadConfig;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.TransferResult;
import oracle.cloudstorage.ftm.TransferState;
import oracle.cloudstorage.ftm.TransferTask;
import oracle.cloudstorage.ftm.UploadConfig;
import oracle.cloudstorage.ftm.exception.ClientException;
import oracle.cloudstorage.ftm.model.CloudContainer;

public class ArchiveStorageDemo {
	private static final String className = ArchiveStorageDemo.class.getSimpleName();
	private static final String demoAccountPropertiesFilepath = "my-account.properties";
	private static final Logger logger = LoggerFactory.getLogger(className);

	private static final String demoContainerPrefix = "ftmapi-demo-arch-";
	private String archContainerName;
	private FileTransferManager manager = null;
	private File file = null;
	private static final String restoreDirPath = "restoredfiles";

	public static void main(String[] args) throws Exception {
		SetupSamplesLog.initLogging(className + ".log");
		ArchiveStorageDemo demo = new ArchiveStorageDemo();
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
				prop.getProperty("password").toCharArray(),
				prop.getProperty("service-name"), prop.getProperty("service-url"), prop.getProperty("identity-domain"));
		try {
			file = createTempFile();
			manager = FileTransferManager.getDefaultFileTransferManager(auth);
			String syncDemoObject = file.getName() + "-" + System.currentTimeMillis();
			archContainerName = demoContainerPrefix + System.currentTimeMillis();
			syncUploadToNonExistingContainer(archContainerName, syncDemoObject);

			archContainerName = demoContainerPrefix + System.currentTimeMillis();

			syncUploadToExistingContainer(archContainerName, syncDemoObject);
			syncDownload(archContainerName, syncDemoObject);

			String asyncDemoObject = file.getName() + "-" + System.currentTimeMillis();
			asyncUploadToExistingContainer(archContainerName, asyncDemoObject);
			asyncDownload(archContainerName, asyncDemoObject);

		} catch (ClientException ce) {
			System.out.println("Operation failed. " + ce.getMessage());
			ce.printStackTrace();
		} finally {
			if (manager != null) {
				manager.shutdown();
			}
		}

	}

	private void syncUploadToNonExistingContainer(String containerName, String objectName) {
		logger.info("Uploading to non-existing archive container using synchronous upload API ...");
		UploadConfig uploadConfig = new UploadConfig();
		uploadConfig.setStorageClass(CloudStorageClass.Archive);
		TransferResult uploadResult = manager.upload(uploadConfig, containerName, objectName, file);
		logger.info("Upload completed. Result:" + uploadResult.toString());
	}

	private void syncUploadToExistingContainer(String containerName, String objectName) {
		logger.info("Uploading to an existing archive container using synchronous upload API ...");

		CloudContainer ccArch = manager.createContainer(containerName, CloudStorageClass.Archive);
		logger.info("Created a new archive container: " + ccArch.toString());
		logger.info("Uploading a file " + file.getAbsolutePath() + " ...");

		UploadConfig uploadConfig = new UploadConfig();
		uploadConfig.setStorageClass(CloudStorageClass.Archive);
		TransferResult uploadResult = manager.upload(uploadConfig, containerName, objectName, file);
		logger.info("Upload completed. Result:" + uploadResult.toString());
	}

	private void asyncUploadToExistingContainer(String containerName, String objectName) {
		logger.info("Uploading to an existing archive container using asynchronous upload API ...");
		UploadConfig uploadConfig = new UploadConfig();
		uploadConfig.setOverwrite(true);
		uploadConfig.setStorageClass(CloudStorageClass.Archive);
		TransferTask<TransferResult> uploadTask = manager.uploadAsync(uploadConfig, containerName, objectName, file);

		logger.info("Waiting for upload task to complete...");
		TransferResult uploadResult = uploadTask.getResult();
		logger.info("Task completed. State:" + uploadResult.getState());
	}

	private void syncDownload(String containerName, String objectName) throws InterruptedException {
		logger.info("Starting synchronous downloading of file ... ");
		File destFile = new File(restoreDirPath + File.separator + objectName);
		DownloadConfig downloadConfig = new DownloadConfig();
		TransferResult downloadResult = manager.download(downloadConfig, containerName, objectName, destFile);
		logger.info("Task completed. State:" + downloadResult.toString());
		TransferState ts = downloadResult.getState();
		while (ts.equals(TransferState.RestoreInProgress)) {
			logger.info("Restore in progress. % completed: " + downloadResult.getRestoreCompletedPercentage());
			Thread.sleep(1 * 60 * 1000); // Wait for 1 mins.
			downloadResult = manager.download(downloadConfig, containerName, objectName, destFile);
			ts = downloadResult.getState();
		}
		logger.info("Download Result:" + downloadResult.toString());
		logger.info("Completed synchronous downloading of file ... ");
	}

	private void asyncDownload(String containerName, String objectName) throws InterruptedException {
		logger.info("Starting asynchronous downloading of file ... ");
		File destFile = new File(restoreDirPath + File.separator + objectName);
		DownloadConfig downloadConfig = new DownloadConfig();
		TransferTask<TransferResult> downloadTask = manager.downloadAsync(downloadConfig, containerName, objectName,
				destFile);
		TransferState ts = TransferState.InProgress;
		while (!ts.equals(TransferState.Completed)) {
			while (!downloadTask.isDone()) {
				// Small sleep time here for checking the download progress.
				Thread.sleep(1 * 60 * 1000); // Wait for 1 mins.
			}
			ts = downloadTask.getResult().getState();
			if (ts.equals(TransferState.RestoreInProgress)) {

				// Longer sleep time here for restore to finish (which takes
				// hours, typically 2 - 4 hours).
				Thread.sleep(1 * 60 * 1000); // Wait for 10 mins.
				downloadTask = manager.downloadAsync(downloadConfig, containerName, objectName, destFile);
				ts = TransferState.InProgress;
			}
		}
		logger.info("Download Result:" + downloadTask.getResult().toString());
		logger.info("Completed asynchronous downloading of file ... ");
	}

	private static File createTempFile() throws IOException {
		File file = File.createTempFile("ftmapi-demo-arch-", ".txt", null);
		file.deleteOnExit();
		String fileContent = "This is a FTM demo file for archive storage. Created at " + System.currentTimeMillis();
		Files.write(file.toPath(), fileContent.getBytes("UTF-8"));
		return file;
	}
}
