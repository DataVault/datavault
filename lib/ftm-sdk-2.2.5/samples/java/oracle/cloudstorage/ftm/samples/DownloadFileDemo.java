package oracle.cloudstorage.ftm.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.DownloadConfig;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.TransferResult;
import oracle.cloudstorage.ftm.exception.ClientException;

public class DownloadFileDemo {
	private static final String className = DownloadFileDemo.class.getSimpleName();
	private static final String demoAccountPropertiesFilepath = "my-account.properties";
	private static final Logger logger = LoggerFactory.getLogger(className);
	private static final String restoreDirPath = "restoredfiles";

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
			// Create the restoreDirPath if required
			File restoreDir = new File(restoreDirPath);
			if (!restoreDir.isDirectory()) {
				restoreDir.mkdir();
			}
			String containerName = "ftmapi-demo";
			String objectName = "fmtest.txt";
			File file = new File(restoreDirPath + File.separator + objectName);
			DownloadConfig downloadConfig = new DownloadConfig();
			logger.info("Downloading file " + file.getName() + " from container " + containerName);
			TransferResult uploadResult = manager.download(downloadConfig, containerName, objectName, file);
			logger.info("Download completed. State:" + uploadResult.getState());
		} catch (ClientException ce) {
			System.out.println("Operation failed. " + ce.getMessage());
		} finally {
			if (manager != null) {
				manager.shutdown();
			}
		}
	}
}
