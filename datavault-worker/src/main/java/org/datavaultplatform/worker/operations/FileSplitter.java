package org.datavaultplatform.worker.operations;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of methods to split a file into several chunks
 * Size of the chunk can be defined into the project configuration file
 *
 */
public class FileSplitter {
    private static final Logger log = LoggerFactory.getLogger(Packager.class);
    
    public static int DEFAULT_CHUNK_SIZE = 500 * 1000 * 1000; // 500MB
    
    public static int BUFFER_SIZE = 8 * 1024; // 8KB
    
    public static String CHUNK_SEPARATOR = "."; // 8KB
    
    /**
     * Will chunk the input file using default chunk size.
     * 
     * @param input  - input File to split
     * @return
     * @throws Exception
     */
    public static File[] spliteFile(File inputFile) throws Exception {
        
        return spliteFile(inputFile, DEFAULT_CHUNK_SIZE, inputFile.getParentFile());
    }
    
    /**
     * Will chunk the input file using default output location i.e. same place as the input file.
     * 
     * @param input  - input File to split
     * @return
     * @throws Exception
     */
    public static File[] spliteFile(File inputFile, long bytesPerChunk) throws Exception {
        
        return spliteFile(inputFile, bytesPerChunk, inputFile.getParentFile());
    }
    
    /**
     * 
     * @param input - input File to split
     * @param chunkSize - size of each chunks
     * @return each chunks into an Array
     * @throws Exception
     */
    public static File[] spliteFile(File inputFile, long bytesPerChunk, File outputDir) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
        long sourceSize = raf.length();
        int numSplits = (int) (sourceSize/bytesPerChunk);
        long remainingBytes = sourceSize % bytesPerChunk;
                
        if(remainingBytes > 0){
            numSplits++;
        }
        File[] chunks = new File[numSplits];
        
        log.debug("sourceSize: "+sourceSize);
        log.debug("numSplits: "+numSplits);
        log.debug("remainingBytes: "+remainingBytes);
        
        for(int destIx=1; destIx <= numSplits; destIx++) {
            String chunkFileName = inputFile.getName()+CHUNK_SEPARATOR+destIx;
            File outputFile = new File (outputDir, chunkFileName);
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(outputFile));

            log.debug("Creating chunk file: "+chunkFileName);
            
            boolean isLastChunk = destIx == numSplits;
            
            if (isLastChunk && remainingBytes > 0){
                log.debug("Writing last extra bytes: "+remainingBytes);
                if (remainingBytes > BUFFER_SIZE){
                    long numReads = remainingBytes/BUFFER_SIZE;
                    long numRemainingRead = remainingBytes % BUFFER_SIZE;
                    for(int i=0; i<numReads; i++) {
                        readWrite(raf, bw, BUFFER_SIZE);
                    }
                    if(numRemainingRead > 0) {
                        readWrite(raf, bw, numRemainingRead);
                    }
                }else {
                    readWrite(raf, bw, remainingBytes);
                }
            } else {
                long numReads = bytesPerChunk/BUFFER_SIZE;
                long numRemainingRead = bytesPerChunk % BUFFER_SIZE;
                for(int i=0; i<numReads; i++) {
                    readWrite(raf, bw, BUFFER_SIZE);
                }
                if(numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            }
            
            chunks[destIx-1] = outputFile;
            
            bw.close();
        }
        raf.close();
        
        return chunks;
    }
    
    public static File recomposeFile(File[] inputFiles, File outputFile) throws Exception {
        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(outputFile));
        
        for (File inputFile : inputFiles){
            RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
            long fileSize = inputFile.length();
            
            log.debug("Add chunk file: "+inputFile.getName());
            
            long numReads = fileSize/BUFFER_SIZE;
            long numRemainingRead = fileSize % BUFFER_SIZE;
            for(int i=0; i<numReads; i++) {
                readWrite(raf, bw, BUFFER_SIZE);
            }
            if(numRemainingRead > 0) {
                readWrite(raf, bw, numRemainingRead);
            }
            raf.close();
        }
        bw.close();
        
        return outputFile;
    }
    
    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
    }
}
