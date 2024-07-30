package org.datavaultplatform.worker.tasks;

import org.datavaultplatform.common.task.Context;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveChunkInfo;
import org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils;

import java.io.File;

/*
 * This class was introduced to help mock the decrypt and file digest check.
 * There were problems using mockito to mock
 *  static methods in a multithreaded program.
 */
public class RetrievedChunkFileChecker {
    
    public void decryptAndCheckTarFile(Context context, RetrieveChunkInfo chunkInfo) throws Exception {
        int chunkNumber = chunkInfo.chunkNumber();
        byte[] iv = chunkInfo.iv();
        File chunkFile = chunkInfo.chunkFile();
        String encChunkDigest = chunkInfo.encChunkDigest();
        String chunkDigest = chunkInfo.chunkDigest();
        RetrieveUtils.decryptAndCheckTarFile("chunk-"+chunkNumber, context, iv, chunkFile, encChunkDigest, chunkDigest);
    }
}
