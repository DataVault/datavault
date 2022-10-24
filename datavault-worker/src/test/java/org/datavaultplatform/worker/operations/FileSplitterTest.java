package org.datavaultplatform.worker.operations;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@Slf4j
public class FileSplitterTest {
    private static String bigFileResources;
    private static File tempDir;
    
    @BeforeAll
    public static void setUpClass() {
        String resourcesDir = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources";

        bigFileResources = resourcesDir + File.separator + "big_data";
        
        tempDir = new File(resourcesDir, "tmp");
    }

    @BeforeEach
    public void setUp() {
        Logger restClientLogger = (Logger) LoggerFactory.getLogger(FileSplitter.class);
        restClientLogger.setLevel(Level.DEBUG);

        try {
            tempDir.mkdir();
        } catch(SecurityException se) {
            fail(se.getMessage());
        }
    }
    
    @Test
    public void testSplit50MBFile() {
        System.out.println( "\nStart testSplit50MBFile");
        try {
            File inputFile = new File(bigFileResources, "50MB_file");
            
            long csumInputFile = 0;
            try {
                csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
            } catch (IOException ioe) {
                fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
            }
            
            long bytesPerChunk = 1000 * 1000; // 1MB
            
            long start = System.currentTimeMillis();
            
            File[] chunks = FileSplitter.splitFile(inputFile, bytesPerChunk, tempDir);
            
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total time: " + elapsedTimeSec + "sec");
            
            assertEquals(50, chunks.length);
            
            for(File chunk : chunks){
                assertEquals(bytesPerChunk, chunk.length());
            }
            
            start = System.currentTimeMillis();
            
            File recomposedFile = new File(tempDir, "50MB_file");
            FileSplitter.recomposeFile(chunks, recomposedFile);
            
            elapsedTimeMillis = System.currentTimeMillis()-start;
            elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total recomposing time: " + elapsedTimeSec + "sec");
            
            long csumOutputFile = 0;
            try {
                csumOutputFile = FileUtils.checksum(recomposedFile, new CRC32()).getValue();
            } catch (IOException ioe) {
                fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
            }

            assertEquals(csumInputFile, csumOutputFile);
            
            for(File chunk : chunks){
                chunk.delete();
            }
            recomposedFile.delete();
        } catch(Exception se) {
            log.error("Unexpected Exception", se);
            fail(se.getMessage());
        }
    }
    

    
    @Test
    public void testSplit50MBFileWithExtraBytes() {
        System.out.println( "\nStart testSplit50MBFileWithExtraBytes");
        try {
            File inputFile = new File(bigFileResources, "50MB_file");
            
            long csumInputFile = 0;
            try {
                csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
            } catch (IOException ioe) {
                fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
            }
            
            long bytesPerChunk = 1500 * 1000; // 1.5MB
            
            long start = System.currentTimeMillis();
            
            File[] chunks = FileSplitter.splitFile(inputFile, bytesPerChunk, tempDir);
            
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total time: " + elapsedTimeSec + "sec");
            
            assertEquals(34, chunks.length);
            
            int pos;
            long lastBytesChunk = 500 * 1000; // 500kB
            for(pos=0; pos <= 33; pos++){
                File chunk = chunks[pos];
                if(pos < 33){
                    assertEquals(bytesPerChunk, chunk.length());
                }
                else {
                    assertEquals(lastBytesChunk, chunk.length());
                }
            }
            
            start = System.currentTimeMillis();
            
            File recomposedFile = new File(tempDir, "50MB_file");
            FileSplitter.recomposeFile(chunks, recomposedFile);
            
            elapsedTimeMillis = System.currentTimeMillis()-start;
            elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total recomposing time: " + elapsedTimeSec + "sec");
            
            long csumOutputFile = 0;
            try {
                csumOutputFile = FileUtils.checksum(recomposedFile, new CRC32()).getValue();
            } catch (IOException ioe) {
                fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
            }

            assertEquals(csumInputFile, csumOutputFile);
            
            assertEquals(csumInputFile, csumOutputFile);
            
            for(File chunk : chunks){
                chunk.delete();
            }
            recomposedFile.delete();
        } catch(Exception se) {
            log.error("unexpected exception", se);
            fail(se.getMessage());
        }
    }
    
    @Test
    @Disabled
    //@Category(org.datavaultplatform.SlowTest.class)
    public void testSplit500MBFile() {
        System.out.println( "\nStart testSplit500MBFile");
        try{
            File inputFile = new File(bigFileResources, "500MB_file");
            
            long bytesPerChunk = 100 * 1000 * 1000; // 100MB
            
            long start = System.currentTimeMillis();
            
            File[] chunks = FileSplitter.splitFile(inputFile, bytesPerChunk, tempDir);
            
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total time: " + elapsedTimeSec + "sec");
            
            assertEquals(5, chunks.length);
            
            for(File chunk : chunks){
                assertEquals(bytesPerChunk, chunk.length());
                chunk.delete();
            }
        } catch(Exception se) {
            log.error("unexpected exception", se);
            fail(se.getMessage());
        }
    }
    
    @Test
    @Disabled
    //@Category(org.datavaultplatform.SlowTest.class)
    public void testSplit500MBFileWithExtraBytes() {
        System.out.println( "\nStart testSplit500MBFileWithExtraBytes");
        try {
            File inputFile = new File(bigFileResources, "500MB_file");
            
            long bytesPerChunk = 150 * 1000 * 1000; // 150MB
            
            long start = System.currentTimeMillis();
            
            File[] chunks = FileSplitter.splitFile(inputFile, bytesPerChunk, tempDir);
            
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total time: " + elapsedTimeSec + "sec");
            
            assertEquals(4, chunks.length);
            
            int pos;
            long lastBytesChunk = 50 * 1000 * 1000; // 50MB
            for(pos=0; pos < chunks.length; pos++){
                File chunk = chunks[pos];
                if(pos < 3){
                    assertEquals(bytesPerChunk, chunk.length());
                }
                else {
                    assertEquals(lastBytesChunk, chunk.length());
                }
                chunk.delete();
            }
        } catch(Exception se) {
            log.error("unexpected exception", se);
            fail(se.getMessage());
        }
    }
    
    @Test
    //@Category(org.datavaultplatform.SlowTest.class)
    @Disabled
    public void testSplitGigaBytesFile() {
        System.out.println( "\nStart testSplitGigaBytesFile");
        try{
            File inputFile = new File(bigFileResources, "1.5GB_file");
            
            long bytesPerChunk = 500 * 1000 * 1000; // 500MB
            
            long start = System.currentTimeMillis();
            
            File[] chunks = FileSplitter.splitFile(inputFile, bytesPerChunk, tempDir);
            
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total time: " + elapsedTimeSec + "sec");
            
            assertEquals(3, chunks.length);
            
            for(File chunk : chunks){
                assertEquals(bytesPerChunk, chunk.length());
                chunk.delete();
            }
        } catch(Exception se) {
            log.error("unexpected exception", se);
            fail(se.getMessage());
        }
    }
    
    @Test
    @Disabled
    //@Category(org.datavaultplatform.SlowTest.class)
    public void testSplitGigaBytesFileWithExtraBytes() {
        System.out.println( "\nStart testSplitGigaBytesFileWithExtraBytes");
        try {
            File inputFile = new File(bigFileResources, "1.5GB_file");
            
            long bytesPerChunk = 1000 * 1000 * 1000; // 1GB
            
            long start = System.currentTimeMillis();
            
            File[] chunks = FileSplitter.splitFile(inputFile, bytesPerChunk, tempDir);
            
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total time: " + elapsedTimeSec + "sec");
            
            assertEquals(2, chunks.length);
            
            int pos;
            long lastBytesChunk = 500 * 1000 * 1000; // 500MB
            assertEquals(bytesPerChunk, chunks[0].length());
            chunks[0].delete();
            assertEquals(lastBytesChunk, chunks[1].length());
            chunks[1].delete();
        } catch(Exception se) {
            log.error("unexpected exception", se);
            fail(se.getMessage());
        }
    }
    
    @Test
    @Disabled
    //@Category(org.datavaultplatform.SlowTest.class)
    public void testSplitHugeBytesFileAndRecompose() {
        System.out.println( "\nStart testSplitHugeBytesFileAndRecompose");
        try{
            File inputFile = new File(bigFileResources, "15GB_file");
            
            long csumInputFile = 0;
            try {
                csumInputFile = FileUtils.checksum(inputFile, new CRC32()).getValue();
            } catch (IOException ioe) {
                fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
            }
            
            long bytesPerChunk = 1000 * 1000 * 1000; // 1GB
            
            long start = System.currentTimeMillis();
            
            File[] chunks = FileSplitter.splitFile(inputFile, bytesPerChunk, tempDir);
            
            long elapsedTimeMillis = System.currentTimeMillis()-start;
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total chunking time: " + elapsedTimeSec + "sec");
            
            assertEquals(15, chunks.length);
            
            for(File chunk : chunks){
                assertEquals(bytesPerChunk, chunk.length());
            }
            
            start = System.currentTimeMillis();
            
            File recomposedFile = new File(tempDir, "15GB_file");
            FileSplitter.recomposeFile(chunks, recomposedFile);
            
            elapsedTimeMillis = System.currentTimeMillis()-start;
            elapsedTimeSec = elapsedTimeMillis/1000F;
            System.out.println( "Total recomposing time: " + elapsedTimeSec + "sec");
            
            long csumOutputFile = 0;
            try {
                csumOutputFile = FileUtils.checksum(recomposedFile, new CRC32()).getValue();
            } catch (IOException ioe) {
                fail("Unexpected Exception thrown while getting checksum of output file: " + ioe);
            }

            assertEquals(csumInputFile, csumOutputFile);
        } catch(Exception se) {
            log.error("unexpected exception", se);
            fail(se.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(tempDir);
        } catch(IOException ex){
            fail(ex.getMessage());
        }
    }

}
