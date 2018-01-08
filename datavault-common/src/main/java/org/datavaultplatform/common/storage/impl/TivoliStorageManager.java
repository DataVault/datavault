package org.datavaultplatform.common.storage.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import org.datavaultplatform.common.io.Progress;
import org.datavaultplatform.common.storage.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.Verify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TivoliStorageManager extends Device implements ArchiveStore {

    private static final Logger logger = LoggerFactory.getLogger(TivoliStorageManager.class);

    // todo : can we change this to COPY_BACK?
    public Verify.Method verificationMethod = Verify.Method.LOCAL_ONLY;

    public TivoliStorageManager(String name, Map<String,String> config) throws Exception  {
        super(name, config);

        // Unpack the config parameters (in an implementation-specific way)
        // Actually I can't think of any parameters that we need.
    }
    
//  @Override
//  public long getUsableSpace() throws Exception {
//      File file = new File(rootPath);
//      return file.getUsableSpace();
//  }
    
    @Override
    public long getUsableSpace() throws Exception {
    		long retVal = 0;

        ProcessBuilder pb = new ProcessBuilder("dsmc", "query", "filespace");

        Process p = pb.start();

        // This class is already running in its own thread so it can happily pause until finished.
        p.waitFor();

        if (p.exitValue() != 0) {
            logger.info("Filespace output failed.");
            logger.info(p.getErrorStream().toString());
            logger.info(p.getOutputStream().toString());
            throw new Exception("Filespace output failed.");
        }
        
        // need to parse the output to get the usable space value
        // this looks like it will be a bit of a pain
        // I suspect the format might be quite awkward
        // (need to wait till I can actually connect to a tsm before I can do this)

        return retVal;
    }

//	@Override
//	public void retrieve(String path, File working, Progress progress) throws Exception {
//		Path absolutePath = getAbsolutePath(path);
//		File file = absolutePath.toFile();
//	  
//		if (file.isFile()) {
//			FileCopy.copyFile(progress, file, working);
//		} else if (file.isDirectory()) {
//			FileCopy.copyDirectory(progress, file, working);
//		}
//	}
    
    @Override
    public void retrieve(String path, File working, Progress progress) throws Exception {
    		// todo : monitor progress
    	
    		// do we need to check if the selected retrieval space has enough free space? (is the file bagged and tarred atm or is the actual space going to be different?)
    		// actually the Deposit  / Retreive worker classes check the free space it appears if we get here we don't need to check
    	
    		// The working file appears to be bagged and tarred when we get here
    		// in the local version of this class the FileCopy class adds info to the progess object
    		// I don't think we need to use the patch at all in this version 

    		logger.info("Retrieve command is " + "dsmc", "retrieve", working.getAbsolutePath(), "-description=" + "?");
        ProcessBuilder pb = new ProcessBuilder("dsmc", "retrieve", working.getAbsolutePath(), "-description=" + "?");

        Process p = pb.start();

        // This class is already running in its own thread so it can happily pause until finished.
        p.waitFor();

        if (p.exitValue() != 0) {
            logger.info("Retrieval of " + working.getName() + " failed. ");
            InputStream error = p.getErrorStream();
            for (int i = 0; i < error.available(); i++) {
            		logger.info("" + error.read());
            }
            throw new Exception("Retrieval of " + working.getName() + " failed. ");
        }
    }

//	@Override
//	public String store(String path, File working, Progress progress) throws Exception {
//		Path absolutePath = getAbsolutePath(path);
//		File retrieveFile = absolutePath.resolve(working.getName()).toFile();
//	
//		if (working.isFile()) {
//			FileCopy.copyFile(progress, working, retrieveFile);
//		} else if (working.isDirectory()) {
//			FileCopy.copyDirectory(progress, working, retrieveFile);
//		}
//	
//		return working.getName();
//	}
    
    @Override
    public String store(String path, File working, Progress progress) throws Exception {

        // todo : monitor progress

        // Note: generate a uuid to be passed as the description. We should probably use the deposit UUID instead (do we need a specialised archive method)?
        // Just a thought - Does the filename contain the deposit uuid? Could we use that as the description?
        String randomUUIDString = UUID.randomUUID().toString();
        
        // check we have enough space to store the data (is the file bagged and tarred atm or is the actual space going to be different?)
        // actually the Deposit  / Retreive worker classes check the free space it appears if we get here we don't need to check
        
        // The working file appears to be bagged and tarred when we get here
		// in the local version of this class the FileCopy class adds info to the progess object
		// I don't think we need to use the patch at all in this version 
        logger.info("Store command is " + "dsmc", "archive", working.getAbsolutePath(), "-description=" + randomUUIDString);
        ProcessBuilder pb = new ProcessBuilder("dsmc", "archive", working.getAbsolutePath(), "-description=" + randomUUIDString);

        Process p = pb.start();

        // This class is already running in its own thread so it can happily pause until finished.
        p.waitFor();

        if (p.exitValue() != 0) {
            logger.info("Deposit of " + working.getName() + " failed. ");
            logger.info(p.getErrorStream().toString());
            logger.info(p.getOutputStream().toString());
            throw new Exception("Deposit of " + working.getName() + " failed. ");
        }

        return randomUUIDString;
    }
    


    @Override
    public Verify.Method getVerifyMethod() {
        return verificationMethod;
    }
    
//  @Override
//  public long getUsableSpaceToolBox() throws Exception {
//  		long retVal = 0;
//
//  		ITSMSession session = null;
//  		session = this.getTSMSession();
//  		IFilespace filespace = session.retrieveFilespace(FILESPACE_NAME);
//  		retVal = filespace.getCapacity();
//  		this.closeTSMSession(session);
//      return retVal;
//  }
    
//  private ITSMSession getTSMSession() throws ToolboxException{
//	ITSMSession session = null;
//try {
//    // Initializing a single-threaded environment
//    Toolbox.initSingleThreading();
//    
//    // Connecting to the server with custom connection options
//    ConnectionOptions connectionOptions = new ConnectionOptions();
//    connectionOptions.setConfigFile(CONFIG_FILE);
//    session = Toolbox.createSession(connectionOptions);
//    
//    System.out.println("Connecting successful.");
//} catch (ToolboxException e) {
//    Throwable cause = e.getCause();
//    if (cause instanceof TSMException
//            && ((TSMException) cause).getReturnCode() == TsmReturnCodes.DSM_RC_NO_PASS_FILE) {
//        System.out.println("No password file, generating one.");
//        
//        ConnectionOptions connectionOptions = new ConnectionOptions();
//        connectionOptions.setConfigFile(CONFIG_FILE);
//        connectionOptions.setPassword(PASSWORD);
//        session = Toolbox.createSession(connectionOptions);
//    } else {
//        e.printStackTrace();
//        throw e;
//    }
//}
//
//return session;
//}

//private void closeTSMSession(ITSMSession session) {
//	if (session) {
//		// Disconnecting
//		session.disconnect();
//    System.out.println("Disconnecting successful.");
//        
//    // Cleaning up the environment
//    Toolbox.cleanUp();
//	}
//}
//
//public String storeToolbox(String path, File working, Progress progress) throws Exception {
//
//// todo : monitor progress
//
//// Note: generate a uuid to be passed as the description. We should probably use the deposit UUID instead (do we need a specialised archive method)?
//// Just a thought - Does the filename contain the deposit uuid? Could we use that as the description?
//String randomUUIDString = UUID.randomUUID().toString();
//String NODE_NAME = "TOOLBOX";
//String PASSWORD = "PASSWORD";
////String INPUT_FILE_NAME = "inputs/archive.txt";
//String OWNER = "Owner";
//String LOW_LEVEL_NAME = "/OBJECT1";
//String HIGH_LEVEL_NAME = "/ARCHIVE";
//String FILESPACE_NAME = "/ARCHIVEFS";
//ITSMSession session = null;
//try {
//		session = this.getTSMSession();
//    
//    // Registering the filespace
//    IFilespace filespace = session.retrieveFilespace(FILESPACE_NAME);
//    
//    // Checking if filespace exists
//    if (filespace == null) {
//        System.out.printf("Warning: '%s' does not exists.\n", FILESPACE_NAME);
//        return;
//    }
//    
//    // Creating a new archive object
//    IArchiveObject archive = filespace.createArchiveFile(
//            HIGH_LEVEL_NAME,
//            LOW_LEVEL_NAME,    
//            OWNER);             
//    archive.setDescription(archive.getLowLevelName());
//    
//    try {
//        // Binding the management class
//        archive.bindManagementClass();
//    } catch (ToolboxException e) {
//        // Checking if archive copy group exists in the bound management class
//        if (e.getReturnCode() == ToolboxErrorCodes.TSM_EXCEPTION ) {
//            TSMException ex = (TSMException) e.getCause();
//            if (ex.getReturnCode() == TsmReturnCodes.DSM_RC_TL_NOACG) {
//                System.out.println(ex.getMessage());
//                return;
//            }
//        }
//        return;
//    }
//    File file = new File(INPUT_FILE_NAME);
//    FileInputStream in = new FileInputStream(file);
//    
//    // Setting object size
//    archive.setEstimatedSize(file.length());
//    
//    // Beginning a transaction
//    session.beginTransaction();
//    try {
//        // Sending the object to the server
//        archive.send(in);
//        session.commit();
//        System.out.printf("Archive object '%s%s%S' has been sent.\n",
//                FILESPACE_NAME, HIGH_LEVEL_NAME, LOW_LEVEL_NAME);
//    } catch(ToolboxException e) {
//        // Rolling back the current transaction
//        session.rollback();
//    }           
//} catch (ToolboxException e) {
//    e.printStackTrace();
//} finally {
//    this.closeTSMSession(session);
//}
//
//return randomUUIDString;
//}
    
//  public void retrieveToolBox(String path, File working, Progress progress) throws Exception {
//	
//}
}
