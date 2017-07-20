package oracle.cloudstorage.ftm.samples;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.DownloadConfig;
import oracle.cloudstorage.ftm.FileObjectTransferConfig;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.MultiFileTransferResult;
import oracle.cloudstorage.ftm.exception.ClientException;

public class DownloadMultiFileDemo {
	private static final String className = UploadMultiFileDemo.class.getSimpleName();
	private static final String demoAccountPropertiesFilepath = "my-account.properties";
	private static final Logger logger = LoggerFactory.getLogger(className);
	private static String testFilesDir = "/tmp/10kbfiles";

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

			String containerName = "ftmapi-demo";

			DownloadConfig downloadConfig = new DownloadConfig();

			List<FileObjectTransferConfig> fileObjectTransferConfigs = new ArrayList<FileObjectTransferConfig>();
			for (int i = 1; i <= 5; i++) {
				String filename = "10kbFile" + i;
				fileObjectTransferConfigs
						.add(new FileObjectTransferConfig(new File(testFilesDir + "/" + filename), filename));
			}
			logger.info("Downloading files from " + containerName + " ... ");
			MultiFileTransferResult downloadResult = manager.downloadMultipleFiles(downloadConfig, containerName,
					fileObjectTransferConfigs);
			logger.info("Download completed. " + downloadResult.toString());
		} catch (ClientException ce) {
			System.out.println("Operation failed. " + ce.getMessage());
		} finally {
			if (manager != null) {
				manager.shutdown();
			}
		}
	}
}
