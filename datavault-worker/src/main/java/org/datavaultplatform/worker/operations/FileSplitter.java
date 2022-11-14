package org.datavaultplatform.worker.operations;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * Set of methods to split a file into several chunks
 * Size of the chunk can be defined into the project configuration file
 *
 */
@Slf4j
public class FileSplitter {

    public static final int DEFAULT_CHUNK_SIZE = 500 * 1000 * 1000; // 500MB

    public static final int BUFFER_SIZE = 8 * 1024; // 8KB


    public static final String CHUNK_SEPARATOR = "."; // 8KB
    
    /**
     * Will chunk the input file using default chunk size.
     * 
     * @param inputFile  - input File to split
     * @return
     * @throws Exception
     */
    public static File[] splitFile(File inputFile) throws Exception {
        
        return splitFile(inputFile, DEFAULT_CHUNK_SIZE, inputFile.getParentFile());
    }
    
    /**
     * Will chunk the input file using default output location i.e. same place as the input file.
     * 
     * @param inputFile  - input File to split
     * @param bytesPerChunk - size of each chunk
     * @return
     * @throws Exception
     */
    public static File[] splitFile(File inputFile, long bytesPerChunk) throws Exception {
        
        return splitFile(inputFile, bytesPerChunk, inputFile.getParentFile());
    }
    
    /**
     * 
     * @param inputFile - input File to split
     * @param bytesPerChunk - size of each chunks
     * @param outputDir - output directory where the chunk files will be created
     * @return each chunk file into an Array
     * @throws Exception if there is a problem
     */
    public static File[] splitFile(File inputFile, long bytesPerChunk, File outputDir) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(inputFile, "r")) {
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
                File outputFile = new File(outputDir, chunkFileName);
                try (BufferedOutputStream bw = new BufferedOutputStream(
                    new FileOutputStream(outputFile))) {

                    log.debug("Creating chunk file: "+chunkFileName);

                    boolean isLastChunk = destIx == numSplits;

                    if (isLastChunk && remainingBytes > 0){
                        log.debug("Writing last extra bytes: " + remainingBytes);
                        if (remainingBytes > BUFFER_SIZE){
                            multipleReadWrites(remainingBytes, raf, bw);
                        }else {
                            readWriteNumBytes(raf, bw, (int) remainingBytes);
                        }
                    } else {
                        multipleReadWrites(bytesPerChunk, raf, bw);
                    }
                }
                chunks[destIx-1] = outputFile;
            }
            Stream.of(chunks).forEach(f -> log.debug("Chunk file: [{}/{}]", f.getName(), f.length()));
            return chunks;
        }
    }

    public static File recomposeFile(File[] inputFiles, File outputFile) throws Exception {
        try (BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            for (File inputFile : inputFiles){
                try (RandomAccessFile raf = new RandomAccessFile(inputFile, "r")) {
                    long fileSize = inputFile.length();

                    log.debug("Add chunk file: "+inputFile.getName());
                    log.debug("Buffer Size: " + BUFFER_SIZE);

                    multipleReadWrites(fileSize, raf, bw);
                }
            }
        }
        return outputFile;
    }

    private static void multipleReadWrites(long size, RandomAccessFile raf, OutputStream os) throws IOException {
        long numReads = size / BUFFER_SIZE;
        int numRemainingRead = (int) size % BUFFER_SIZE;
        for(int i=0; i<numReads; i++) {
            readWriteNumBytes(raf, os, BUFFER_SIZE);
        }
        if(numRemainingRead > 0) {
            readWriteNumBytes(raf, os, numRemainingRead);
        }
    }

    static void readWriteNumBytes(RandomAccessFile raf, OutputStream os, int numBytes) throws IOException {
        Assert.isTrue(numBytes <= BUFFER_SIZE, () -> "numBytes cannot be greater than BUFFER_SIZE");
        byte[] buf = new byte[numBytes];
        raf.readFully(buf);
        os.write(buf);
    }
}
