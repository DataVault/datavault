package oracle.cloudstorage.ftm.samples;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.cloudstorage.ftm.CloudStorageClass;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.exception.ClientException;
import oracle.cloudstorage.ftm.model.CloudContainer;

public class ContainerDemo {
	private static final String className = ContainerDemo.class.getSimpleName();
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

			String containerName = "ftmapi-demo-std-" + System.currentTimeMillis();
			boolean containerExists = manager.containerExists(containerName);
			logger.info("Before container " + containerName + " creation: containerExists() result:" + containerExists);
			CloudContainer cc = manager.createContainer(containerName, CloudStorageClass.Standard);
			containerExists = manager.containerExists(containerName);
			logger.info("After container " + containerName + " creation: containerExists() result:" + containerExists);
			logger.info("Created standard container result: " + cc.toString());
			cc = manager.getContainer(containerName);
			logger.info("Get standard container result: " + cc.toString());

			String archContainerName = "ftmapi-demo-arch-" + System.currentTimeMillis();
			containerExists = manager.containerExists(archContainerName);
			logger.info(
					"Before container " + archContainerName + " creation: containerExists() result:" + containerExists);
			CloudContainer ccArch = manager.createContainer(archContainerName, CloudStorageClass.Archive);
			containerExists = manager.containerExists(archContainerName);
			logger.info(
					"After container " + archContainerName + " creation: containerExists() result:" + containerExists);
			logger.info("Create archive container result: " + ccArch.toString());

			ccArch = manager.getContainer(archContainerName);
			logger.info("Get archive container result: " + ccArch.toString());

		} catch (ClientException ce) {
			System.out.println("Operation failed. " + ce.getMessage());
		} finally {
			if (manager != null) {
				manager.shutdown();
			}
		}

	}
}
