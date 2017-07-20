package oracle.cloudstorage.ftm.samples;

import java.io.File;

import oracle.cloudstorage.ftm.CloudStorageClass;
import oracle.cloudstorage.ftm.FileTransferAuth;
import oracle.cloudstorage.ftm.FileTransferManager;
import oracle.cloudstorage.ftm.TransferResult;
import oracle.cloudstorage.ftm.UploadConfig;
import oracle.cloudstorage.ftm.exception.ClientException;

public class UploadFileDemo2 {
	public static void main(String[] args) throws Exception {
		FileTransferAuth auth = new FileTransferAuth(
				"john.doe@oracle.com", // user name
				"Welcome1!".toCharArray(), // password
				"storage", //  service name
				"https://storagedomain.storage.oraclecloud.com", // service URL
				"storagedomain" // identity domain
		);
		FileTransferManager manager = null;
		try {
			manager = FileTransferManager.getDefaultFileTransferManager(auth);
			String containerName = "mycontainer";
			String objectName = "foo.txt";
			File file = new File("/tmp/foo.txt");
			UploadConfig uploadConfig = new UploadConfig();
			uploadConfig.setOverwrite(true);
			uploadConfig.setStorageClass(CloudStorageClass.Standard);
			System.out.println("Uploading file " + file.getName() + " to container " + containerName);
			TransferResult uploadResult = manager.upload(uploadConfig, containerName, objectName, file);
			System.out.println("Upload completed successfully.");
			System.out.println("Upload result:" + uploadResult.toString());
		} catch (ClientException ce) {
			System.out.println("Upload failed. " + ce.getMessage());
		} finally {
			if (manager != null) {
				manager.shutdown();
			}
		}

	}
}
